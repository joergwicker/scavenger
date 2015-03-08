package scavenger

import scala.concurrent.Future
import scavenger.categories.formalccc

case class ResourcePair[X, Y](x: Resource[X], y: Resource[Y])
extends Resource[(X, Y)] {
  def identifier = formalccc.Couple(x.identifier, y.identifier)
  def difficulty = Cheap
  def cachingPolicy = CachingPolicy.Nowhere
  def compute(ctx: Context): Future[(X, Y)] = {
    ctx.submit(x).zip(ctx.submit(y))
  }
}