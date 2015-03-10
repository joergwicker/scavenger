package scavenger.backend

import akka.Actor
import scala.concurrent.{Future, Promise, ExecutionContext}
import scavenger._

trait Scheduler extends Actor 
with ResourceEvaluator
with ActorContextProvider {
  protected def mustScheduleHere(
    policy: CachingPolicy, 
    difficulty: Difficulty
  ): Boolean

  /**
   * Decomposes a potentially complex job and schedules 
   * individual parts for computation.
   */
  def schedule(job: Resource[Any]): Future[Value[Any]] = {
    if (mustScheduleHere(job.cachingPolicy, job.difficulty)) {
      // no choice, we are forced to schedule it right here,
      // we can not delegate it anyway, so there is no
      // reason to try to simplify it.
      for(x <- computeHere(job)) 
        yield Value(job.identifier, x, job.cachingPolicy)
    } else {
      for {
        simplifiedResource <- simplify(job)
        finalResult <- computeSimplified(simplifiedResource)
      } yield Value(job.identifier, finalResult, CachingPolicy.Nowhere)
    }
  }

  /**
   * Simplify a resource such that the resulting resource can
   * be handled in one piece (e.g. sent to a single worker node)
   */
  private def simplify(job: Resource[Any]): Future[Resource[Any]] = {
    job.simplify(getContext, mustScheduleHere)
  }
}