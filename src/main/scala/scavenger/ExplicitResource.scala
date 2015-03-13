package scavenger

import scala.concurrent.{ExecutionContext, Future}

/**
 * Explicit computations are computations that do not require any 
 * computations. These are either constant values or backed up
 * values stored in files.
 */
trait ExplicitComputation[+X] extends Computation[X] {
  def getIt(implicit execCtx: ExecutionContext): Future[X]
  def compute(ctx: Context) = getIt(ctx.executionContext)
  def difficulty = Cheap
  def simplify(
    ctx: Context, 
    mustBeReplaced: (CachingPolicy, Difficulty) => Boolean
  ): Future[Computation[X]] = Future(this)(ctx.executionContext)
}