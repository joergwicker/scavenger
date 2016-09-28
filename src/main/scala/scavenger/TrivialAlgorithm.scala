package scavenger 
import scala.concurrent.{ExecutionContext, Future}

sealed trait TrivialAlgorithm[-X, +Y] {

  /** Transforms a trivial job with parameter `X` into a trivial job with
    * parameter `Y`. The application is formal, no actual computation takes
    * place until `eval` is called on the resulting `TrivialJob`.
    */
  def apply(t: TrivialJob[X]): TrivialJob[Y]
}

abstract class TrivialAtomicAlgorithm[-X, +Y] extends TrivialAlgorithm[X, Y] {

  def apply(c: TrivialJob[X]): TrivialJob[Y] = TrivialApply(this, c)

  /** 
   * An upper bound for `size(x) / size(y)`, where `x` is some input of 
   * type `X`, `y` is the output of type `Y`.
   */
  def sizeFactorUpperBound: Double

  /** Computes an `Y` from an `X`, using `ctx`.
    *
    * This is the actual hard work done by `TrivialAlgorithm`, everything
    * else is just formal composition and delegation.
    */
  def compute(x: X)(implicit ctx: BasicContext): Future[Y]
}

/** Do-nothing morphism.
  *
  * Sometimes useful in conjunction with `TrivialCouple`.
  * For example, `TrivialCouple(TrivialId, TrivialId)` takes one input, 
  * and duplicates it, so that the two copies can then be fed into two 
  * different algorithms.
  */
case class TrivialId[X]() extends TrivialAlgorithm[X, X] {

  def apply(t: TrivialJob[X]): TrivialJob[X] = t
}

/** Formal composition of two algorithms.
  *
  * Reminder: `(g o f) x = g (f x)`. Note the order: it is right-to-left, as
  * is common in the mathematical notation.
  */
case class TrivialComposition[-X, Y, +Z](
  second: TrivialAlgorithm[Y, Z],
  first: TrivialAlgorithm[X, Y]
) extends TrivialAlgorithm[X, Z] {
  def apply(x: TrivialJob[X]): TrivialJob[Z] = second(first(x))
}

case class TrivialCouple[-X, +A, +B](
  left: TrivialAlgorithm[X, A],
  right: TrivialAlgorithm[X, B]
) extends TrivialAlgorithm[X, (A, B)] {
  def apply(x: TrivialJob[X]): TrivialJob[(A, B)] = 
    TrivialPair(left(x), right(x))
}

case class TrivialProj1[X, -Y](
  sizeFactorUpperBound: Double = java.lang.Math.nextDown(1.0d)
) extends TrivialAtomicAlgorithm[(X, Y), X] {
  def compute(xy: (X, Y))(implicit ctx: BasicContext): Future[X] = {
    import ctx.executionContext
    Future { xy._1 }
  }
}

case class TrivialProj2[-X, Y](
  sizeFactorUpperBound: Double = java.lang.Math.nextDown(1.0d)
) extends TrivialAtomicAlgorithm[(X, Y), Y] {
  def compute(xy: (X, Y))(implicit ctx: BasicContext): Future[Y] = {
    import ctx.executionContext
    Future { xy._2 }
  }
}

case class TrivialCurry1[A, -B, +Z](
  algorithm: TrivialAlgorithm[(A, B), Z],
  firstArgument: TrivialJob[A]
) extends TrivialAlgorithm[B, Z] {
  def apply(secondArgument: TrivialJob[B]): TrivialJob[Z] = 
    algorithm(TrivialPair(firstArgument, secondArgument))
}

case class TrivialCurry2[-A, B, +Z](
  algorithm: TrivialAlgorithm[(A, B), Z],
  secondArgument: TrivialJob[B]
) extends TrivialAlgorithm[A, Z] {
  def apply(firstArgument: TrivialJob[A]): TrivialJob[Z] = 
    algorithm(TrivialPair(firstArgument, secondArgument))
}