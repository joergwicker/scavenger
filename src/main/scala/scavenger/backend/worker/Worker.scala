package scavenger.backend.worker

import akka.actor._
import akka.pattern.pipe
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scavenger._
import scavenger.backend._
import scavenger.backend.LastMessageTimeMonitoring._
import scavenger.categories.formalccc

/** Actor that controls the computation on
  * a worker node.
  *
  * A worker node obtains the address of a
  * master node from a seed node.
  * Whenever it is not occupied, it sends
  * a job request to the master. Then it processes
  * the job, and sends back the result.
  *
  * @since 2.1
  * @author Andrey Tyukin
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
with DemilitarizedZone
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
    case InternalJob(label, job) => {
      log.info("Got a job! " + job + " switching into working state")
      context.become(working)
      provideComputationContext.submit(job).map{
        x => FinalResult(label, x)
      } pipeTo self
    }
    
    // ask master for the job again, just in case it forgot us somehow
    case r: Reminder if(isRelevant(r)) => {
      log.info("sending a reminder to master")
      master ! WorkerHere
      remindMyself(35, "keep re-reminding")
    }
    
    case irrelevant: Reminder => { /* ignore */ }

    case Ping => sender ! Echo

  }: Receive) orElse 
  handleExternalRequests orElse 
  handleLocalResponses orElse
  handleHandshakeRemnants orElse
  monitorCache orElse
  reportUnexpectedMessages
  
  /** When a worker is occupied, it does not react on
    * anything except `Ping` requests.
    *
    * As soon as it gets an `FinalResult` from itself,
    * it sends it to master, and gets back into `awaitingJob` behavior.
    */
  private val working: Receive = ({
    case r: Reminder => { remindMyself(60, "re-reminding while working") }
    case Ping => sender ! Echo
    case JobsAvailable => {} // ignore
    case NoJobsAvailable => {} // ignore
    case FinalResult(label, value) => {
      log.debug(
        "Received FinalResult from " + sender + ", " +
        "Computed solution for {}, sending it to master, " + 
        "switching back into `awaitingJob` mode.", label
      )
      context.become(awaitingJob) // switch back into `awaitingJob` mode
      master ! InternalResult(label, value)
    }
    case InternalJob(label, job) => {
      log.error(
        "Received a job while already being at work, " +
        "id = " + label
      )
      throw new AssertionError(
        "Worker received a new job while working on another job. " +
        "Must be a bug in the scavenger.backend.master.LoadBalancer"
      )
    }
  }: Receive) orElse 
  handleExternalRequests orElse 
  handleLocalResponses orElse 
  handleHandshakeRemnants orElse 
  monitorCache orElse
  reportUnexpectedMessages

  /** Behavior for reporting unexpected messages */
  private def reportUnexpectedMessages: Receive = ({
    case unexpectedMessage => {
      unexpectedMessage match {
        case akka.actor.Status.Failure(exc) => {
          val sw = new java.io.StringWriter()
          val pw = new java.io.PrintWriter(sw)
          exc.printStackTrace(pw)
          val stackTrace = sw.toString()
          log.error("Exception caught in Worker: " + 
            exc.getMessage + "\n" + stackTrace
          )
        }
      }
      log.error(
        "Received something unexpected: " + unexpectedMessage + " " + 
        "The class of this thing is: " + unexpectedMessage.getClass + " "
      )
    }
  }: Receive)
}

/** The worker object describes various kinds of messages that
  * can be sent by a worker.
  */
object Worker {
  def props(seedPath: ActorPath) = Props(classOf[Worker], seedPath)
  
  private[backend] case object WorkerHere extends HandshakeMessage
  private[backend] case object NeedJob
  private[Worker] case class FinalResult(label: InternalLabel, x: Any)
}
