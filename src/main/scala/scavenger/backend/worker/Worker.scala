package scavenger.backend.worker

import akka.actor._
import akka.pattern.pipe
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scavenger._
import scavenger.backend._
import scavenger.backend.LastMessageTimeMonitoring._

/**
 * Actor that controls the computation on
 * a worker node.
 * 
 * A worker node obtains the address of a 
 * master node from a seed node.
 * Whenever it is not occupied, it sends 
 * a job request to the master.
 */
class Worker(seedPath: ActorPath) 
extends Actor 
with ActorLogging
with SeedJoin
with MasterJoin
with Remindable
with BruteForceEvaluator
with WorkerScheduler
with WorkerCache
with ExternalInterface
with ContextProvider {

  import context.dispatcher
  import Worker._

  // initialization phase requires a reminder that triggers the connection
  // establishing behavior.
  remindMyself(1, "Try to connect to master")

  // The initial connection phase
  def receive = connectingToSeed(
    seedPath,    // where to send the handshake
    WorkerHere,  // what exactly to send
    connectingToMaster(
      WorkerHere, 
      WorkerHere,
      awaitingJob  // what to do after connection is established
    )
  )

  private val awaitingJob: Receive = ({
    
    // make use of the opportunity! Get the job!
    case JobsAvailable => 
      log.info("Notified of new jobs, let's see if I can get one")
      master ! WorkerHere
    
    // nope, didn't get the last job...
    // go over to slow polling 
    // (just in case JobsAvailable messages get lost for some reason)
    case NoJobsAvailable => 
      log.info("Master said there are no jobs available. Will check later.")
      remindMyself(20, "slow polling for jobs as backup-strategy")
    
    // handle jobs from master (simply let them wait for results from the
    // cache)
    // The original `id` of the job is stored in the closure
    case InternalJob(job: Resource[Any]) => {
      log.info("Got a job! " + job + " switching into working state")
      provideComputationContext.submit(job).map{
        x => InternalResult(job.identifier, x)
      } pipeTo self
      context.become(working)
    }
    
    // ask master for the job again, just in case it forgot us somehow
    case r: Reminder if(isRelevant(r)) => {
      log.info("sending a reminder to master")
      master ! WorkerHere
      remindMyself(35, "keep re-reminding")
    }
    
    case Ping => sender ! Echo

  }: Receive) orElse handleHandshakeRemnants
  
  /**
   * When a worker is occupied, it does not react on
   * anything except `Ping` requests.
   * 
   * As soon as it gets an `InternalResult` from itself,
   * it sends it to master, and gets back into `awaitingJob` behavior.
   */
  private val working: Receive = ({
    case r: Reminder => { remindMyself(60, "re-reminding while working") }
    case Ping => sender ! Echo
    case JobsAvailable => {} // ignore
    case NoJobsAvailable => {} // ignore
    case res @ InternalResult(id, value) => {
      master ! res
      master ! WorkerHere
      context.unbecome() // switch back into `awaitingJob` mode
    }
    case InternalJob(job) => {
      log.error(
        "Received a job while already being at work, " +
        "id = " + job.identifier
      )
      throw new AssertionError(
        "Worker received a new job while working on another job. " +
        "Must be a bug in the scavenger.backend.master.LoadBalancer"
      )
    }
  }: Receive) orElse handleHandshakeRemnants
}

/**
 * The worker object describes various kinds of messages that
 * can be sent by a worker.
 */
object Worker {
  def props(seedPath: ActorPath) = Props(classOf[Worker], seedPath)
  
  private[backend] case object WorkerHere extends HandshakeMessage
  private[backend] case object NeedJob
}