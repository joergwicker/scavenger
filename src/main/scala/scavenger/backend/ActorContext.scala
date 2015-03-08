package scavenger.backend

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.language.postfixOps
import scavenger.{Context, Resource}

/**
 * Implementation of a context that sends jobs to 
 * an actor (Worker or Master).
 *
 * Such contexts interact with the referenced actor by 
 * sending `Job`s and getting `Result`s from the actor.
 *
 * Such a context can be used by a `Resource` that is 
 * being executed on a node, or by a user who submits jobs.
 */
class ActorContext(
  private val actorRef: ActorRef,
  private val execCtx: ExecutionContext
) extends Context {

  import ActorContext._

  private implicit val 
    practicallyInfinite: Timeout = Timeout(1000000 hours)
  implicit def executionContext = execCtx
  def submit[X](job: Resource[X]): Future[X] = {
    for (Result(x) <- (actorRef ? Job(job)).mapTo[Result[X]]) yield x
  }
}

object ActorContext {
  /**
   * Job submitted by a user or a locally run Resource
   */
  private[backend] case class Job[X](job: Resource[X])

  /**
   * Response to a request containing a `Job`
   */
  private[backend] case class Result[X](x: X)
}