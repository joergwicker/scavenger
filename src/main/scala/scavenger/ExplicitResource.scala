package scavenger

import scala.concurrent.{ExecutionContext, Future}

/** Represents explicit results that do not require any 
  * actual computation, but might take some time to load.
  * 
  * These are either constant values or backed up
  * values stored in files.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
trait ExplicitComputation[+X] extends Computation[X] {
  def getExplicitValue(implicit execCtx: ExecutionContext): Future[X]
  def compute(ctx: Context) = getExplicitValue(ctx.executionContext)
  def difficulty = Cheap
  def simplify(
    ctx: Context, 
    mustBeReplaced: (CachingPolicy, Difficulty) => Boolean
  ): Future[Computation[X]] = Future(this)(ctx.executionContext)
}
