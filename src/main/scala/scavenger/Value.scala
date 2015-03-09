package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scavenger.categories.formalccc

/**
 * A resource with an explicitly specified value of type `X`
 * that does not require any computation at all.
 */
case class Value[X](
  identifier: formalccc.Elem,
  value: X,
  cachingPolicy: CachingPolicy
) extends Resource[X] {
  def compute(ctx: Context): Future[X] = {
    import ctx.executionContext
    Future{ value }
  }
  def difficulty = Cheap
  def directDependencies = Nil
  def simplify(replacement: List[Future[Resource[_]]])(
    implicit exec: ExecutionContext
  ) = {
    if (!replacement.isEmpty) {
      throw new SimplificationException(
        "Value resource " + identifier,
        "Do not require any replacement-subresources, but received %d != 0".
          format(replacement.size)
      )
    } else {
      Future(this)
    }
  }
}