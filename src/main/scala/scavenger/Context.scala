package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/**
 * A `Context` represents a some kind of compute
 * node that provides computations for the 
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
 */
trait Context {
  implicit def executionContext: ExecutionContext
  
  /**
   * Guarantees to return the same value as if `job.compute(this)` 
   * have been called instead.
   */
  def submit[X](job: Computation[X]): Future[X]
  
  /**
   * Similar to `submit`, but the resulting value is 
   * wrapped into a `Value`-`Computation`
   */
  def asExplicitComputation[X](job: Computation[X]): 
    Future[ExplicitComputation[X]]

  /**
   * Dumps list with identifiers of the cached intermediate
   * results, if this `Context` is backed by something that 
   * actually has a cache.
   *
   * Mostly for testing purposes, so that we can make sure that
   * the caching behavior is as we expect.
   */
  private[scavenger] def dumpCacheKeys: 
    List[scavenger.categories.formalccc.Elem] 
}