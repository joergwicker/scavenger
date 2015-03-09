package scavenger.backend

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.collection.mutable.HashMap
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.ClassTag
import scavenger.{Context, Resource}
import scavenger.categories.formalccc

/**
 * Implementation of a context that sends jobs to 
 * an actor on the same node (Worker or Master).
 *
 * Such contexts interact with the referenced actor by 
 * sending `Job`s and getting `Result`s from the actor.
 *
 * Such a context can be used by a `Resource` that is 
 * being executed on a node, or by a user who submits jobs
 * to the `Master` node.
 *
 * This class is conceptually similar to what Akka's
 * `PromiseActorRef` is supposed to do, but we did not attempt
 * to "optimize" it in any way.
 */
class ActorContext(
  private val actorRef: ActorRef,
  private val execCtx: ExecutionContext
) extends Actor with Context {

  import ActorContext._

  private val asked = HashMap.empty[formalccc.Elem, Promise[Any]]

  implicit def executionContext = execCtx

  def submit[X](job: Resource[X])(implicit tag: ClassTag[X]): Future[X] = {
    val p = Promise[Any]
    asked(job.identifier) = p
    actorRef ! Job(job)
    p.future.mapTo[X]
  }

  def receive = ({
    case Result(id, x) => asked(id).success(x)
  }: Receive)
}

object ActorContext {
  /**
   * Job submitted by a user or a locally run Resource
   */
  private[backend] case class Job[+X](job: Resource[X])

  /**
   * Response to a request containing a `Job`
   */
  private[backend] case class Result[+X](id: formalccc.Elem, x: X)
}