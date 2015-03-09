package scavenger

import scala.concurrent.Future
import scala.reflect.ClassTag
import scavenger.categories.formalccc

/**
 * A resource with an explicitly specified value of type `X`
 * that does not require any computation at all.
 */
case class Value[X](
  identifier: formalccc.Elem,
  value: X,
  cachingPolicy: CachingPolicy
)(implicit classTag: ClassTag[X]) extends Resource[X] {
  def compute(ctx: Context): Future[X] = {
    import ctx.executionContext
    Future{ value }
  }
  def difficulty = Cheap
}

