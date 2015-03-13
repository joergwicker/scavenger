package scavenger.util

/*
import akka.actor.ActorLogging
import scala.collection.mutable.HashMap
import scala.concurrent.Promise

/**
 * `Promise`-backed cache that returns 
 * `Future[A]`s immediately. 
 */
trait Cache {
  
  private hashMap

  def containsKey[X](identifier: Identifier[X]) 
  private val subjobPromises = 
    new HashMap[ComputationIdentifier, Promise[Any]]
  
  /**
   * Cache with byValue or promise-backed computations
   * 
   * Read and Write access for subclasses of this trait.
   */
  protected[scavenger] val _cache = 
    new HashMap[ComputationIdentifier, AtomicComputation[Any]]
  
  /**
   * Read-only access for Computation[X]
   */
  protected[scavenger] def cache: ComputationCache = _cache
  
  /**
   * Mark a computation in the cache as available.
   * 
   * Internally, stores a promise that is tied to the computation in the cache.
   * The computation becomes actually available when the `fulfillPromisedSubjob` 
   * is called for the same computation identifier.
   */
  protected[scavenger] def promiseSubjob(
    job: Computation[Any]
  ): Unit = {
    log.info("promising: " + job)
    if (cache.contains(job.id)) {
      throw new 
        IllegalStateException("Attempted to promise " + job + " twice!")
    }
    val p = Promise[Any]()
    subjobPromises(job.id) = p 
    _cache(job.id) = AtomicComputation(job.id, p, job.isCachedOnWorkers)
    log.info("Current cache: " + cache.values.mkString(","))
  }
  
  /**
   * Fulfill a promise to compute the explicit result of a subjob.
   */
  protected[scavenger] def fulfillPromisedSubjob(
    resultId: ComputationIdentifier, 
    result: Any
  ): Unit = {
    subjobPromises(resultId).success(result)
    subjobPromises.remove(resultId)
  }
  
  /**
   * Fulfill a promise with a `ByValueComputation` (which is expected to
   * contain the correct identifier)
   */
  protected[scavenger] def fulfillPromisedSubjob(bv: ByValueComputation[Any]): 
    Unit = {
    fulfillPromisedSubjob(bv.id, bv.value)
  }
  
  /**
   * Deletes computation from cache if it's marked temporary
   */
  def tryFree(id: ComputationIdentifier): Unit = {
    log.info("Trying to free " + id + " (temporary=" + isTemporary(id) + ")")
    if (isTemporary(id)) _cache.remove(id)
  }
  
  def tryFreeAll(requiredCleanup: List[ComputationIdentifier]): Unit = {
    requiredCleanup foreach tryFree
  }
}

object PromiseBackedCache {
  case class Cleanup(requiredCleanup: List[ComputationIdentifier])
}
*/