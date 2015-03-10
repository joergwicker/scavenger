package scavenger.backend

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.collection.mutable.HashMap
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import scavenger.{Context, Resource}
import scavenger.categories.formalccc

/**
 * Implementation of a context that sends jobs to 
 * an actor on the same node (Worker or Master).
 *
 * Classes of this type provide a bridge between the `Actor`
 * world and the `Future` world, similarly to the `PromiseActorRef`,
 * used in the ask-pattern.
 * 
 * Such contexts interact with the referenced actor by 
 * sending `Job`s and getting `Result`s from the actor.
 *
 * Such a context can be used by a `Resource` that is 
 * being executed on a node, or by a user who submits jobs
 * to the `Master` node.
 */
class ActorContext(
  private val actorRef: ActorRef,
  implicit val executionContext: ExecutionContext
) extends Context {

  import ActorContext._

  def submit[X](job: Resource[X]): Future[X] = {
    // That's totally like Hawking's "grey holes":
    // Promises are thrown into the "black hole", Futures escape...
    val p = Promise[X]
    actorRef ! ExternalInterface.Compute(job, p)
    p.future
  }

}