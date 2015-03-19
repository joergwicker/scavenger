package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scavenger.categories.formalccc

/** A distinguished type of morphisms that is responsible
  * for application of functions to input arguments.
  *
  * It is used in the formulation of partially applied
  * computations.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
case class Eval[X, Y](difficulty: Difficulty) 
extends AtomicAlgorithm[(X=>Y, X), Y] {
  def identifier = formalccc.Eval
  def apply(fx: (X => Y, X), ctx: Context): Future[Y] = {
    import ctx.executionContext
    val (f, x) = fx
    Future{ f(x) }
  }
}
