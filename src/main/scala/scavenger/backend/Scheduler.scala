package scavenger.backend

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import scala.concurrent.{Future, Promise, ExecutionContext}
import scala.collection.mutable.HashMap
import scavenger._
import scavenger.categories.formalccc

/** Scheduler decomposes complex dependency trees into simple manageable
  * tasks, and schedules these tasks.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
trait Scheduler extends Actor with ActorLogging
with SimpleComputationExecutor
with ContextProvider {

  import context.dispatcher
  import Scheduler._

  /** This method determines whether a job must be
    * scheduled on this node, or whether it can
    * be delegated.
        */
  protected def mustScheduleHere(
    policy: CachingPolicy, 
    difficulty: Difficulty
  ): Boolean

  /** This method determines whether a job has to
    * be processed separately before it can be delegated
    * as a subjob of a larger job.
    */
  protected def mustBeSimplified(
    policy: CachingPolicy,
    difficulty: Difficulty
  ): Boolean

  // This hash-map is semi-automatically generated boilerplate, inside of
  // this map, we store promises for futures that require multiple passes
  // through the actor-receive-handlers
  private var internalSchedulerJobId: Long = 0
  private val simplifiedJobs = new HashMap[Long, Promise[OldValue[Any]]]

  /** Decomposes a potentially complex job and schedules
    * individual parts for computation.
    */
  def schedule(job: Computation[Any]): Future[OldValue[Any]] = {
    
    val result = if (mustScheduleHere(job.cachingPolicy, job.difficulty)) {
      // no choice, we are forced to schedule it right here,
      // we can not delegate it anyway, so there is no
      // reason to try to simplify it.
      for(x <- computeHere(job)) 
        yield OldValue(job.identifier, x, job.cachingPolicy)
    } else {
      // Here is where we need the boilerplate-hash-map
      val p = Promise[OldValue[Any]]()
      val ijid = internalSchedulerJobId
      internalSchedulerJobId += 1
      simplifiedJobs(ijid) = p
      simplify(job).map{ j => UnscheduledSimplifiedJob(ijid, j) } pipeTo self
      p.future
    }
   
    result
  }

  def handleScheduling: Receive = ({
    case UnscheduledSimplifiedJob(ijid, j) => {
      computeSimplified(j).map{ 
        r => FinalResultSimplifiedJob(j.identifier, r, ijid) 
      } pipeTo self
    }
    case FinalResultSimplifiedJob(id, res, ijid) => {
      simplifiedJobs(ijid).success(OldValue(id, res, CachingPolicy.Nowhere))
      simplifiedJobs.remove(ijid)
    }
  } : Receive)

  /** Simplify a computation such that the resulting computation can
    * be handled in one piece (e.g. sent to a single worker node)
    */
  private def simplify(job: Computation[Any]): Future[Computation[Any]] = {
    job.simplify(provideComputationContext, mustBeSimplified)
  }
}

object Scheduler {
  
  // The following message types are essentially "semi-automatically generated
  // boilerplate";
  // TODO: try to isolate the phenomenon that leads to this boilerplate, 
  // can't we just build it into the syntax somehow? It's essentially just a 
  // very weird way to paraphrase chaining of futures, but it requires a 
  // separate message type and a separate Receive-handler for each flatMap()
  case class UnscheduledSimplifiedJob(jobId: Long, j: Computation[Any])
  case class FinalResultSimplifiedJob(
    id: formalccc.Elem, 
    finalResult: Any,
    internalJobId: Long
  )
}
