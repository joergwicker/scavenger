package scavenger.backend

import akka.actor.{Actor, ActorLogging}
import scala.collection.mutable.HashMap
import scala.concurrent.{Future, Promise, ExecutionContext}
import scavenger._
import scavenger.categories.formalccc

/** Component of `Master` and `Worker` nodes that is responsible for caching
  * important intermediate results.
  *
  * Manages a cache that maps identifiers to futures.
  * Also responsible for backing results up (on master).
  *
  * @since 2.1
  * @author Andrey Tyukin 
  */
trait Cache extends Actor with ActorLogging with Scheduler {

  import Cache._
  import context.dispatcher

  /** Predicate that determines whether an intermediate result should
    * be cached on this node.
    */
  protected def shouldBeCachedHere(p: CachingPolicy): Boolean

  // The cache stores futures of explicit computations.
  // Explicit computations are either values or data backed up in file.
  // This enables us to send file-handles around, instead of loading 
  // the file locally and then sending the data to another node.
  protected val cache: 
    HashMap[formalccc.Elem, Future[ExplicitComputation[Any]]] = HashMap.empty

  /** Returns alphabetically sorted list of identifiers currently used as keys
    */
  def dumpKeys: List[formalccc.Elem] = cache.keys.toList.sortBy(_.toString)

  /** Gets the final value of the computation, either by retrieving it from
    * cache or by getting it as computation result from the underlying
    * scheduler.
    */
  def getComputed(job: Computation[Any]): Future[Any] = {
    log.debug("Cache.getComputed({})", job)
    if (shouldBeCachedHere(job.cachingPolicy)) {
      // it makes sense to check the cache
      if (cache.isDefinedAt(job.identifier)) {
        // cache hit. Extract the computation, get its value
        for {
          explicit <- cache(job.identifier)
          result <- explicit.getExplicitValue
        } yield result
      } else {
        // it's not in the cache yet. 
        // Get a future from the scheduler, put it into 
        // the cache. 
        // Then unpack the explicit computation and return the
        // explicit value.
        val futValue = schedule(job)
        cache(job.identifier) = futValue
        for {
          res <- futValue
          value <- res.getExplicitValue
        } yield value
      }
    } else {
      // it doesn't even make sense to check the cache,
      // pass it down to scheduler, unpack the result,
      // don't put anything into cache.
      for {
        explicit <- schedule(job)
        value <- explicit.getExplicitValue
      } yield value
    }
  }

  /** Get an equivalent `ExplicitComputation` (either explicit value or
    * a backed up computation)
    */
  def getExplicit(job: Computation[Any]): Future[ExplicitComputation[Any]] = {
    log.debug("Cache.getExplicit({})", job)
    if (shouldBeCachedHere(job.cachingPolicy)) {
      // it makes sense to check the cache
      if (cache.isDefinedAt(job.identifier)) {
        // cache hit. Extract the computation, just return it 
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

  /** Behavior that returns additional information about the state of the
    * cache.
    */
  protected def monitorCache: Receive = ({
    case DumpKeys => sender ! dumpKeys
  }: Receive)
}

/** Contains messages that are specific for the `Cache` */
object Cache {

  /** Message that asks this cache to return a list with all currently stored
    * keys.
    *
    * This type of messages is supposed to be used mainly for testing and
    * monitoring purposes.
    */
  case object DumpKeys
}
