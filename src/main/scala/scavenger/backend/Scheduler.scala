package scavenger.backend

import akka.actor.Actor
import scala.concurrent.{Future, Promise, ExecutionContext}
import scavenger._

trait Scheduler extends Actor 
with ResourceEvaluator
with ContextProvider {

  import context.dispatcher

  /**
   * This method determines whether a job must be 
   * scheduled on this node, or whether it can
   * be delegated.
   */
  protected def mustScheduleHere(
    policy: CachingPolicy, 
    difficulty: Difficulty
  ): Boolean

  /** 
   * This method determines whether a job has to 
   * be processed separately before it can be delegated
   * as a subjob of a larger job.
   */
  protected def mustBeSimplified(
    policy: CachingPolicy,
    difficulty: Difficulty
  ): Boolean

  /**
   * Decomposes a potentially complex job and schedules 
   * individual parts for computation.
   */
  def schedule(job: Resource[Any]): Future[Value[Any]] = {

    val selfName = "" + self
    if (selfName.matches(".*master.*")) {
       println("############################\n" * 3)
       println("self")
       println("Scheduling " + job)
       println(Scheduler.totalJobsScheduled)
       println("############################\n" * 3)
       Scheduler.totalJobsScheduled += 1
    }
    

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
    job.simplify(provideComputationContext, mustBeSimplified)
  }
}

object Scheduler {
  var totalJobsScheduled: Int = 0 // TODO: remove that, and the if-master above
}