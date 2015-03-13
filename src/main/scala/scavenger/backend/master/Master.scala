package scavenger.backend.master

import akka.actor._
import scala.collection.mutable.{HashSet, HashMap}
import scavenger._
import scavenger.backend.{Scheduler => _, _}
import scavenger.categories.formalccc

class Master(val seedPath: ActorPath) 
extends Actor 
with ActorLogging
with SeedJoin 
// put the whole stack together...
with ContextProvider
with LoadBalancer
with MasterScheduler
with MasterCache
with ExternalInterface {

  import Master._

  self ! Reminder(1, "Connect to seed and switch into normal operation mode")

  def receive: Receive = connectingToSeed(
    seedPath,
    MasterHere,
    normalOperationMode
  ) orElse handleExternalRequests

  private def normalOperationMode: Receive = 
    handleExternalRequests orElse
    updatingLastMessageTime(handleWorkerRequests) orElse
    updatingLastMessageTime(handleWorkerResponses) orElse
    handleLocalResponses orElse
    monitorLastMessageTimes orElse
    handleReminders
}

object Master {

  def props(seedPath: ActorPath) = Props(classOf[Master], seedPath)

  /**
   * Handshake message sent to the seed node in the 
   * connection phase.
   */
  private[backend] case object MasterHere extends HandshakeMessage

  /**
   * A message used to delegate a job to the worker node.
   * This kind of communication is initiated by the master.
   */
  private[backend] case class Delegated(job: Computation[Any])

  /**
   * Message for sending important partial results to self
   */
  private[Master] case class CacheThis(
    result: Computation[Any], 
    shouldCache: Boolean,
    shouldBackUp: Boolean
  )

  /**
   * Message for sending simplified partial results to self
   */
  private[Master] case class DelegateThis(
    simplified: Computation[Any]
  )
}
