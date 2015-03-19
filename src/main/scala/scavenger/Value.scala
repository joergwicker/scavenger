package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scavenger.categories.formalccc

/** Explicit value of type `X`
  * that does not require any computation at all.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
case class Value[X](
  identifier: formalccc.Elem,
  value: X,
  cachingPolicy: CachingPolicy
) extends ExplicitComputation[X] {
  def getExplicitValue(implicit execCtx: ExecutionContext) = Future{ value }
}
