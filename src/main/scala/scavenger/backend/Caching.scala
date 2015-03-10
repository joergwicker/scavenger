package scavenger.backend

import akka.actor.Actor
import scala.collection.mutable.HashMap
import scala.concurrent.{Future, Promise, ExecutionContext}
import scavenger.{Resource, ExplicitResource}
import scavenger.categories.formalccc

trait Caching extends Actor with Scheduler {

  import context.dispatcher

  protected def shouldBeCachedHere(p: CachingPolicy): Boolean

  // The cache stores futures of explicit resources.
  // Explicit resources are either values or data backed up in file.
  // This enables us to send file-handles around, instead of loading 
  // the file locally and then sending the data to another node.
  private val cache: 
    HashMap[formalccc.Elem, Future[ExplicitResource[Any]]] = HashMap.empty

  /**
   * Gets the final value of the resource, either by retrieving it from
   * cache or by getting it as computation result from the underlying 
   * scheduler.
   */
  def getComputed(job: Resource[Any]): Future[Any] = {
    if (shouldBeCachedHere(job.cachingPolicy)) {
      // it makes sense to check the cache
      if (cache.isDefinedAt(job.identifier)) {
        // cache hit. Extract the resource, get its value
        for (explicit <- cache(job.identifier)) yield explicit.getIt
      } else {
        // it's not in the cache yet. 
        // Get a future from the scheduler, put it into 
        // the cache. 
        // Then unpack the explicit resource and return the
        // explicit value.
        val futValue = compute(job)
        cache(job.identifier) = futValue
        for (res <- futValue) yield res.getIt
      }
    } else {
      // it doesn't even make sense to check the cache,
      // pass it down to scheduler, unpack the result,
      // don't put anything into cache.
      for (explicit <- compute(job)) yield explicit.getIt
    }
  }

  /**
   * Get an equivalent `ExplicitResource` (either explicit value or 
   * a backed up resource)
   */
  def getExplicit(job: Resource[Any]): Future[ExplicitResource[Any]] = {
    if (shouldBeCachedHere(job.cachingPolicy)) {
      // it makes sense to check the cache
      if (cache.isDefinedAt(job.identifier)) {
        // cache hit. Extract the resource, just return it 
        // (it's already explicit, no need to simplify it any further)
        cache(job.identifier)
      } else {
        // nothing we can do, pass it down to the scheduler
        schedule(job)
      }
    } else {
      // it doesn't even make sense to check the cache,
      // pass it down to scheduler.
      schedule(job)
    }
  }
}