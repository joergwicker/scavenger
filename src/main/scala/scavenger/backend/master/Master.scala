package scavenger.backend

import akka.actor._
import scala.collection.mutable.{HashSet, HashMap}
import scavenger._

class Master(val seedPath: ActorPath) 
extends Actor 
with ActorLogging
with SeedJoin 
with LoadBalancing {

  private val cache: HashMap[formalccc.Elem, Promise[Resource[_]]]

  def receive = ({
    case _ => ??? // TODO
  }: Receive)

  val running: Receive = ({

    // Job coming from a context-implementation
    case Job(resource, promise) => {
      val caching = resource.cachingPolicy
      val difficulty = resource.difficulty

    }
  }: Receive)

}

object Master {

  def props(seedPath: ActorPath) = Props(classOf[Master], seedPath)

  /**
   * Handshake message sent to the seed node in the 
   * connection phase.
   */
  private[backend] case object MasterHere

  /**
   * A message used to delegate a job to the worker node.
   * This kind of communication is initiated by the master.
   */
  private[backend] case class Delegated(job: Resource[Any])

  /**
   * Message for sending important partial results to self
   */
  private[Master] case class CacheThis(
    result: Resource[Any], 
    shouldCache: Boolean,
    shouldBackUp: Boolean
  )

  /**
   * Message for sending simplified partial results to self
   */
  private[Master] case class DelegateThis(
    simplified: Resource[Any]
  )
}