package scavenger.backend

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.collection.mutable.HashMap
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import scavenger._
import scavenger.categories.formalccc
import scavenger.backend.Cache.DumpKeys

/** Implementation of a context that interfaces with
  * the reactive ActorSystem-model.
  *
  * It translates method calls into messages and sends them
  * to an actor on the same node (Worker or Master).
  *
  * Classes of this type provide a bridge between the `Actor`
  * world and the `Future` world, similarly to the `PromiseActorRef`,
  * used in the ask-pattern.
  *
  * This context translates method calls into messages by 
  * sending mutable (!) promises to the actor. 
  * To ensure that the mutable promise does to spread across the
  * actor system, a `DemilitarizedZone` component awaits the 
  * messages on the other side.
  *
  * Such a context can be used by a `Computation` that is
  * being executed on a node, or by a user who submits jobs
  * to the `Master` node.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
class ReactiveContext(
  private val actorRef: ActorRef,
  implicit val executionContext: ExecutionContext
) extends Context {

  def submit[X](job: Computation[X]): Future[X] = {
    // That's kind of like Hawking's "grey holes":
    // Promises are thrown into the "black hole", Futures escape...
    val p = Promise[Any]
    actorRef ! DemilitarizedZone.Compute(job, p)
    p.future.map{ 
      a => a.asInstanceOf[X] 
    }
  }

  def asExplicitComputation[X](job: Computation[X]): 
    Future[ExplicitComputation[X]] = {
    val p = Promise[ExplicitComputation[Any]]
    actorRef ! DemilitarizedZone.GetExplicitComputation(job, p)
    p.future.map{ 
      a => a.asInstanceOf[ExplicitComputation[X]] 
    }
  }

  /** Asks the underlying actor to dump content of it's cache.
    *
    * Intended to be used for testing purposes.
    */
  private[scavenger] def dumpCacheKeys: List[formalccc.Elem] = {
    implicit val to = Timeout(60 seconds)
    Await.result(
      (actorRef ? DumpKeys).mapTo[List[formalccc.Elem]],
      60 seconds
    )
  }
}
