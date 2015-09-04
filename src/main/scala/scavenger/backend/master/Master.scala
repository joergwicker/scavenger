package scavenger.backend.master

import akka.actor._
import scala.collection.mutable.{HashSet, HashMap}
import scavenger._
import scavenger.backend.{Scheduler => _, _}
import scavenger.categories.formalccc

/** The central `Master` node of the Scavenger backend.
  *
  * It receives `Computation`-value requests from the client application,
  * and coordinates the `Worker` nodes to obtain a result in an 
  * efficient manner.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
class Master(val seedPath: ActorPath) 
extends Actor 
with ActorLogging
with SeedJoin 
// put the whole stack together...
with ContextProvider
with LoadBalancer
with MasterScheduler
with MasterCache
with DemilitarizedZone 
with UnexpectedMessageHandler {

  import Master._

  self ! Reminder(1, "Connect to seed and switch into normal operation mode")

  def receive: Receive = connectingToSeed(
    seedPath,
    MasterHere,
    normalOperationMode
  ) orElse handleExternalRequests orElse 
  handleScheduling orElse 
  handleUnexpectedMessages

  private def normalOperationMode: Receive = 
    handleExternalRequests orElse
    updatingLastMessageTime(handleWorkerRequests) orElse
    updatingLastMessageTime(handleWorkerResponses) orElse
    handleScheduling orElse
    handleLocalResponses orElse
    monitorLastMessageTimes orElse
    handleReminders orElse
    monitorCache orElse
    handleUnexpectedMessages
}

/** Defines `props` used to construct `Master` actors, 
  * and master-specific handshake messages
  */
object Master {

  def props(seedPath: ActorPath) = Props(classOf[Master], seedPath)

  /** Handshake message sent to the seed node in the
    * connection phase.
    */
  private[backend] case object MasterHere extends HandshakeMessage

  /** A message used to delegate a job to the worker node.
    * This kind of communication is initiated by the master.
    */
  private[backend] case class Delegated(job: Computation[Any])
}
