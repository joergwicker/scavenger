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
    new HashMap[ResourceIdentifier, Promise[Any]]
  
  /**
   * Cache with byValue or promise-backed resources
   * 
   * Read and Write access for subclasses of this trait.
   */
  protected[scavenger] val _cache = 
    new HashMap[ResourceIdentifier, AtomicResource[Any]]
  
  /**
   * Read-only access for Resource[X]
   */
  protected[scavenger] def cache: ResourceCache = _cache
  
  /**
   * Mark a resource in the cache as available.
   * 
   * Internally, stores a promise that is tied to the resource in the cache.
   * The resource becomes actually available when the `fulfillPromisedSubjob` 
   * is called for the same resource identifier.
   */
  protected[scavenger] def promiseSubjob(
    job: Resource[Any]
  ): Unit = {
    log.info("promising: " + job)
    if (cache.contains(job.id)) {
      throw new 
        IllegalStateException("Attempted to promise " + job + " twice!")
    }
    val p = Promise[Any]()
    subjobPromises(job.id) = p 
    _cache(job.id) = AtomicResource(job.id, p, job.isCachedOnWorkers)
    log.info("Current cache: " + cache.values.mkString(","))
  }
  
  /**
   * Fulfill a promise to compute the explicit result of a subjob.
   */
  protected[scavenger] def fulfillPromisedSubjob(
    resultId: ResourceIdentifier, 
    result: Any
  ): Unit = {
    subjobPromises(resultId).success(result)
    subjobPromises.remove(resultId)
  }
  
  /**
   * Fulfill a promise with a `ByValueResource` (which is expected to
   * contain the correct identifier)
   */
  protected[scavenger] def fulfillPromisedSubjob(bv: ByValueResource[Any]): 
    Unit = {
    fulfillPromisedSubjob(bv.id, bv.value)
  }
  
  /**
   * Deletes resource from cache if it's marked temporary
   */
  def tryFree(id: ResourceIdentifier): Unit = {
    log.info("Trying to free " + id + " (temporary=" + isTemporary(id) + ")")
    if (isTemporary(id)) _cache.remove(id)
  }
  
  def tryFreeAll(requiredCleanup: List[ResourceIdentifier]): Unit = {
    requiredCleanup foreach tryFree
  }
}

object PromiseBackedCache {
  case class Cleanup(requiredCleanup: List[ResourceIdentifier])
}
*/