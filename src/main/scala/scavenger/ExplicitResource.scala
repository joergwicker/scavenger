package scavenger

import scala.concurrent.{ExecutionContext, Future}

/**
 * Explicit resources are resources that do not require any 
 * computations. These are either constant values or backed up
 * values stored in files.
 */
trait ExplicitResource[+X] extends Resource[X] {
  def getIt(implicit execCtx: ExecutionContext): Future[X]
  def compute(ctx: Context) = getIt(cxt.executionContext)
  def difficulty = Cheap
  def simplify(
    cxt: Context, 
    mustBeReplaced: (CachingPolicy, Difficulty) => Boolean
  ): Future[Resource[X]] = Future(this)(ctx.executionContext)
}