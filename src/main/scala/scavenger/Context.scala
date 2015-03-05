package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/**
 * A `Context` represents a some kind of compute
 * node that provides resources for the 
 * computation.
 * The most important property of a `Context` is
 * that `Resource`s can be submitted to it as jobs,
 * and are then guaranteed to produce the same 
 * results as if `Resource.compute(ctx)` has
 * been called on this `Context`.
 * This makes it possible for `Context`s to delegate
 * some of the work to other contexts (e.g. other
 * compute nodes of the cluster, with different 
 * performance and different state of cache).
 */
trait Context {
  implicit def executionContext: ExecutionContext
  def submit[X](job: Resource[X]): Future[X]
}