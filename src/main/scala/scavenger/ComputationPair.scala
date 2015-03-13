package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scavenger.categories.formalccc

case class ComputationPair[X, Y](x: Computation[X], y: Computation[Y])
extends Computation[(X, Y)] {
  def identifier = formalccc.Couple(x.identifier, y.identifier)
  def difficulty = Cheap
  def cachingPolicy = CachingPolicy.Nowhere
  def compute(ctx: Context): Future[(X, Y)] = {
    ctx.submit(x).zip(ctx.submit(y))
  }
  def simplify(
    ctx: Context, 
    mustBeReplaced: (CachingPolicy, Difficulty) => Boolean
  ): Future[Computation[(X, Y)]] = {
    import ctx.executionContext
    for {
      newX <- x.simplifySelfIfNecessary(ctx, mustBeReplaced)
      newY <- y.simplifySelfIfNecessary(ctx, mustBeReplaced)
    } yield ComputationPair(x, y)
  }
}