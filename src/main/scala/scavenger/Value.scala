package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scavenger.categories.formalccc

/**
 * A computation with an explicitly specified value of type `X`
 * that does not require any computation at all.
 */
case class Value[X](
  identifier: formalccc.Elem,
  value: X,
  cachingPolicy: CachingPolicy
) extends ExplicitComputation[X] {
  def getIt(implicit execCtx: ExecutionContext) = Future{ value }
}