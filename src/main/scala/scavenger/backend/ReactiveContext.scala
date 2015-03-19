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

/**
 * Implementation of a context that interfaces with
 * the reactive ActorSystem-model.
 * It translates method calls into messages and sends them
 * to an actor on the same node (Worker or Master).
 *
 * Classes of this type provide a bridge between the `Actor`
 * world and the `Future` world, similarly to the `PromiseActorRef`,
 * used in the ask-pattern.
 * 
 * Such contexts interact with the referenced actor by 
 * sending `Job`s and getting `Result`s from the actor.
 *
 * Such a context can be used by a `Computation` that is 
 * being executed on a node, or by a user who submits jobs
 * to the `Master` node.
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

  private[scavenger] def dumpCacheKeys: List[formalccc.Elem] = {
    implicit val to = Timeout(60 seconds)
    Await.result(
      (actorRef ? DumpKeys).mapTo[List[formalccc.Elem]],
      60 seconds
    )
  }
}
