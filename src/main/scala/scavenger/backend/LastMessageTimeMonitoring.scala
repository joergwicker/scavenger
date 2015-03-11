package scavenger.backend

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable
import scala.collection.mutable.{HashSet, HashMap}
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Trait that helps to monitor when the last message
 * has been received from remote node. 
 */
trait LastMessageTimeMonitoring extends Actor {

  import context.dispatcher
  import LastMessageTimeMonitoring._

  private val MaxNoResponseTime = 180000 // millis
  context.system.scheduler.schedule(20 seconds, 20 seconds, self, SendPings)
  context.system.scheduler.schedule(60 seconds, 180 seconds, self, CheckTimes)

  private val pingList: HashSet[ActorRef] = HashSet.empty[ActorRef]
  protected def addToPingList(actorRef: ActorRef): Unit = {
    pingList += actorRef
    actorRef ! Ping
  }

  private val lastMessageTime: mutable.Map[ActorRef, Long] = 
     HashMap.empty[ActorRef, Long] withDefault {x => 0L}

  /**
   * Modifies a `Receive`-behavior, updates the last message time
   * for all monitored actors.
   */
  protected def updatingLastMessageTime(r: Receive): Receive = {
    r andThen { 
      case _ => {
        if (pingList.contains(sender)) {
          lastMessageTime(sender) = System.currentTimeMillis 
        }
      }
    }
  }

  protected def monitorLastMessageTimes: Receive = {
    case SendPings => {
      for (a <- pingList) a ! Ping
    }
    case CheckTimes => {
      for (a <- pingList) {
        val now = System.currentTimeMillis
        if (now - lastMessageTime(a) > MaxNoResponseTime) {
          // node seems dead... Inform the Master node
          self ! RemoteNodeNotResponding(a)
        }
      }
    }
  }
}

object LastMessageTimeMonitoring {

  /**
   * Asks a node whether it's still alive
   */
  case object Ping

  /**
   * If a node is alive, it should respond with an `Echo` to every `Ping`
   */
  case object Echo

  /**
   * Message sent to `Master` node whenever a `Worker` stops 
   * responding to pings.
   */
  case class RemoteNodeNotResponding(actorRef: ActorRef)

  private[LastMessageTimeMonitoring] case object SendPings
  private[LastMessageTimeMonitoring] case object CheckTimes
}