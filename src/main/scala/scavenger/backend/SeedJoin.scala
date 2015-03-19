package scavenger.backend

import akka.actor._
import akka.pattern.pipe
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Implements the behavior of an Actor that tries 
 * to establish connection to the seed node.
 *
 * Once the `ActorPath` of the seed is resolved,
 * the reference to the seed is available as `seedRef`
 * member variable.
 *
 * Used by both `Worker` and `Master` actors.
 */
trait SeedJoin 
extends Actor 
with ActorLogging
with Remindable {

  import SeedJoin._
  import context.dispatcher

  protected var seedRef: ActorRef = _

  /**
   * Behavior of the actor that tries to connect to seed node.
   * 
   * Requires an initial `Reminder` to trigger the active attempts
   * to connect to the seed node.
   */
  def connectingToSeed(
    seedPath: ActorPath,
    msg: HandshakeMessage,
    nextBehavior: Receive
  ): Receive = ({

    // wake up and try to connect with the seed node
    case r: Reminder => if(isRelevant(r)) {
      import context.dispatcher
      if (seedRef == null) {
        context.actorSelection(seedPath).resolveOne(1 minutes) map { 
          (ref: ActorRef) =>
          SeedResolution(Some(ref))
        } recover {
          case e: akka.actor.ActorNotFound => SeedResolution(None)
        } pipeTo self
        remindMyself(30, "trying to resolve seed")
      }
    } else {
      log.info("Received irrelevant reminder: " + r.message)
    }

    // result of resolving seed
    case SeedResolution(Some(ref)) => {
      seedRef = ref
      log.info("Resolved seed")
      seedRef ! msg
      remindMyself(1, "Connected to seed, continue with next stage")
      context.become(nextBehavior)
    }
    
    // could not find the seed node, just try again later...
    case SeedResolution(None) => {
      remindMyself(60, "retry resolve seed")
    }

  }: Receive)

}

object SeedJoin {
  protected case class SeedResolution(seed: Option[ActorRef])
}
