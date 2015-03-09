package scavenger.backend

import akka.actor._
import scavenger.{Resource,Context}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

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
with Remindable {

  import context.dispatcher

  // ############################# STATE #######################################

  // ########################## INITIALIZATION #################################
  remindMyself(1, "Try to connect to master")
  log.info("Trying to wake myself up in 1 second")

  // ############################  BEHAVIOR ####################################

  // The initial connection phase
  import Worker.WorkerHere
  def receive = connectingToSeed(
    seedPath,    // where to send the handshake
    WorkerHere,  // what exactly to send
    connectingToMaster(
      WorkerHere, 
      WorkerHere,
      awaitingJob  // what to do after connection is established
    )
  )

  // phase where no jobs can make any progress without responses or new
  // jobs from master.
  val awaitingJob: Receive = ({
    // TODO
    case _ => ???
  }: Receive)

  // phase with active jobs
  val working: Receive = ({
    // TODO
    case _ => ???
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