package scavenger.backend

import akka.actor.{Actor, ActorLogging}
import scala.collection.mutable.HashMap
import scala.concurrent.{Future, Promise, ExecutionContext}
import scavenger._
import scavenger.categories.formalccc

trait Cache extends Actor with ActorLogging with Scheduler {

  import context.dispatcher

  protected def shouldBeCachedHere(p: CachingPolicy): Boolean

  // The cache stores futures of explicit resources.
  // Explicit resources are either values or data backed up in file.
  // This enables us to send file-handles around, instead of loading 
  // the file locally and then sending the data to another node.
  protected val cache: 
    HashMap[formalccc.Elem, Future[ExplicitResource[Any]]] = HashMap.empty

  /**
   * Gets the final value of the resource, either by retrieving it from
   * cache or by getting it as computation result from the underlying 
   * scheduler.
   */
  def getComputed(job: Resource[Any]): Future[Any] = {
    log.debug("Cache.getComputed({})", job)
    if (shouldBeCachedHere(job.cachingPolicy)) {
      // it makes sense to check the cache
      if (cache.isDefinedAt(job.identifier)) {
        // cache hit. Extract the resource, get its value
        for {
          explicit <- cache(job.identifier)
          result <- explicit.getIt
        } yield result
      } else {
        // it's not in the cache yet. 
        // Get a future from the scheduler, put it into 
        // the cache. 
        // Then unpack the explicit resource and return the
        // explicit value.
        val futValue = schedule(job)
        cache(job.identifier) = futValue
        for {
          res <- futValue
          value <- res.getIt
        } yield value
      }
    } else {
      // it doesn't even make sense to check the cache,
      // pass it down to scheduler, unpack the result,
      // don't put anything into cache.
      for {
        explicit <- schedule(job)
        value <- explicit.getIt
      } yield value
    }
  }

  /**
   * Get an equivalent `ExplicitResource` (either explicit value or 
   * a backed up resource)
   */
  def getExplicit(job: Resource[Any]): Future[ExplicitResource[Any]] = {
    log.debug("Cache.getExplicit({})", job)
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
