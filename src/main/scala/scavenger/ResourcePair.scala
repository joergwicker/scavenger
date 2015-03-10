package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scavenger.categories.formalccc

case class ResourcePair[X, Y](x: Resource[X], y: Resource[Y])
extends Resource[(X, Y)] {
  def identifier = formalccc.Couple(x.identifier, y.identifier)
  def difficulty = Cheap
  def cachingPolicy = CachingPolicy.Nowhere
  def compute(ctx: Context): Future[(X, Y)] = {
    ctx.submit(x).zip(ctx.submit(y))
  }
  def simplify(
    cxt: Context, 
    mustBeReplaced: (CachingPolicy, Difficulty) => Boolean
  ): Future[Resource[X]] = {
    for {
      newX <- x.simplifySelfIfNecessary(ctx, mustBeReplaced)
      newY <- y.simplifySelfIfNecessary(ctx, mustBeReplaced)
    } yield ResourcePair(x, y)
  }
}