package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scavenger.categories.freeccc

/**
 * A distinguished type of morphism that chooses the
 * first component of a product type.
 */
case class Fst[X, Y]() extends AtomicAlgorithm[(X, Y), X] {
  def identifier = freeccc.Fst[X, Y]
  def apply(xy: (X, Y), ctx: Context): Future[X] = {
    import ctx.executionContext
    val (x, y) = xy
    Future{ x }
  }
  def difficulty = Cheap
}

/**
 * A distinguished type of morphisms that chooses the
 * second component of a product type.
 */
case class Snd[X, Y]() extends AtomicAlgorithm[(X, Y), Y] {
  def identifier = freeccc.Snd[X, Y]
  def apply(xy: (X, Y), ctx: Context): Future[Y] = {
    import ctx.executionContext
    val (x, y) = xy
    Future{ y }
  }
  def difficulty = Cheap
}