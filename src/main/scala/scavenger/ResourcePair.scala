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
  def directDependencies = List(x, y)
  def simplify(newDependencies: List[Future[Resource[_]]])(
    implicit exec: ExecutionContext
  ): Future[Resource[(X, Y)]] = {
    if (newDependencies.size == 2) {
      for {
        newX <- newDependencies(0).asInstanceOf[Future[Resource[X]]]
        newY <- newDependencies(1).asInstanceOf[Future[Resource[Y]]]
      } yield {
        ResourcePair(newX, newY)
      }
    } else {
      throw new SimplificationException(
        "Resource pair " + identifier,
        "Need exactly two dependencies, but got %d".format(newDependencies.size)
      )
    }
  }
}