package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/** Represents a compute node that can have various capabilities and 
  * states of the cache.
  *
  * A `Context` represents a some kind of compute
  * node that provides resources for the
  * computation.
  * The most important property of a `Context` is
  * that `Computation`s can be submitted to it as jobs,
  * and are then guaranteed to produce the same
  * results as if `Computation.compute(ctx)` has
  * been called on this `Context`.
  * This makes it possible for `Context`s to delegate
  * some of the work to other contexts (e.g. other
  * compute nodes of the cluster, with different
  * performance and different state of cache).
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
trait Context {
  implicit def executionContext: ExecutionContext
  
  /** Guarantees to return the same value as if `job.compute(this)`
    * has been called instead.
    */
  def submit[X](job: Computation[X]): Future[X]
  
  /** Similar to `submit`, but the resulting value is
    * wrapped as an `ExplicitComputation`, which is 
    * essentially just a value somewhere in memory.
    */
  def asExplicitComputation[X](job: Computation[X]): 
    Future[ExplicitComputation[X]]

  /** Dumps list with identifiers of the cached intermediate
    * results, if this `Context` is backed by something that
    * actually has a cache.
    *
    * Mostly for testing purposes, so that we can make sure that
    * the caching behavior is as we expect.
    */
  private[scavenger] def dumpCacheKeys: 
    List[scavenger.categories.formalccc.Elem] 
}

trait TrivialContext {
  implicit def executionContext: ExecutionContext
  private[scavenger] def loadFromCache[X](id: Identifier): Future[X]
  private[scavenger] def loadFromCache[X](selector: TrivialJob[X]): Future[X]
}

/** 
 * Computation context provided by the worker node, which does not 
 * provide the possibility to look anything up in worker's cache.
 * 
 * Essentially the same as `TrivialContext` (corresponds to the fact that
 * `IrreducibleJob` is a pure marker trait).
 */
trait IrreducibleContext extends TrivialContext {
  
}

trait LocalContext extends IrreducibleContext {
  def submit[X](job: LocalComputation[X]): Future[X]
  def computeValue[X](job: LocalComputation[X]): Future[NewValue[X]]
}

trait DistributedContext extends LocalContext {
  def submit[X](job: DistributedComputation[X]): Future[X]
  def computeValue[X](job: DistributedComputation[X]): Future[NewValue[X]]
}