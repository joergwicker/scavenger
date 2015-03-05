package scavenger

import scala.concurrent.Future

/**
 * A resource with an explicitly specified value of type `X`
 * that does not require any computation at all.
 */
case class Value[X](
  identifier: Identifier[X],
  value: X,
  cachingPolicy: CachingPolicy
) extends Resource[X] {
  def compute(ctx: Context): Future[X] = {
    import ctx.executionContext
    Future{ value }
  }
  def difficulty = Cheap
}

