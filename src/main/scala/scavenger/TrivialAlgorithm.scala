package scavenger 
import scala.concurrent.{ExecutionContext, Future}

sealed trait TrivialAlgorithm[-X, +Y] {

  /** Transforms a trivial job with parameter `X` into a trivial job with
    * parameter `Y`. The application is formal, no actual computation takes
    * place until `eval` is called on the resulting `TrivialJob`.
    */
  def apply(t: TrivialJob[X]): TrivialJob[Y] = TrivialApply(this, t)

  /** Performs the actual computation in the specified context. */
  def compute(x: X)(implicit ctx: BasicContext): Future[Y]

  import TrivialJob.Size

  /** Takes (before, after)-compression size estimation for the input, 
    * returns (before, after)-compression size estimation for
    * the output.
    *
    * Notice: the before-compression size can be modified by curried functions.
    * For example, if `a` is an input of certain size, `b` is an input of 
    * certain size, and `f` is a two-parameter function, then `f(a, _)` will
    * transform the estimation `1 * size(b)` into 
    * `1 * size(b) + 1 * size(a)`. So, the `befor-compression` component 
    * cannot be omitted.
    */
  private[scavenger] def transformSizeEstimates(
    before: Size, 
    after: Size
  ): (Size, Size)
}

abstract class TrivialAtomicAlgorithm[-X, +Y] extends TrivialAlgorithm[X, Y] {

  /** 
   * An upper bound for `size(x) / size(y)`, where `x` is some input of 
   * type `X`, `y` is the output of type `Y`.
   */
  def compressionFactor: Double

  private[scavenger] def transformSizeEstimates(before: Size, after: Size): 
  (Size, Size) = (before, s * compressionFactor)
}

/** Do-nothing morphism.
  *
  * Sometimes useful in conjunction with `TrivialCouple`.
  * For example, `TrivialCouple(TrivialId, TrivialId)` takes one input, 
  * and duplicates it, so that the two copies can then be fed into two 
  * different algorithms.
  */
case class TrivialId[X]() extends TrivialAlgorithm[X, X] {
  private[scavenger] def transformSizeEstimates(before: Size, after: Size): 
  (Size, Size) = (before, after)
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
  def compute(x: X)(implicit ctx: BasicContext): Future[Y] = {
    import ctx.executionContext
    for {
      yIntermedRes <- first.compute(x)
      zRes <- second.compute(yIntermedRes)
    } yield zRes
  }
  private[scavenger] def transformSizeEstimates(before: Size, after: Size): 
  (Size, Size) = {
    val (b, a) = first.transformSizeEstimates(before, after)
    second.transformSizeEstimates(b, a)
  }
}

case class TrivialCouple[-X, +A, +B](
  left: TrivialAlgorithm[X, A],
  right: TrivialAlgorithm[X, B]
) extends TrivialAlgorithm[X, (A, B)] {
  def compute(x: X)(implicit ctx: BasicContext): Future[(A, B)] = {
    import ctx.executionContext
    val lFut = left.compute(x)
    val rFut = right.compute(x)
    for {
      lRes <- lFut
      rRes <- rFut
    } yield (lRes, rRes)
  }
  private[scavenger] def transformSizeEstimates(before: Size, after: Size): 
  (Size, Size) = {
    val (lb, la) = transformSizeEstimates(before, after)
    val (rb, ra) = transformSizeEstimates(before, after)
    /* the `max` appears because we are essentially computing the characteristic
       function of a union of two sets: the DAG-leaf nodes used in lb, and 
       DAG-leaf nodes used in rb.
    */
    (lb.max(rb), la + ra)
  }
}

case class TrivialProj1[X, -Y](
  compressionFactor: Double = java.lang.Math.nextDown(1.0d)
) extends TrivialAtomicAlgorithm[(X, Y), X] {
  def compute(xy: (X, Y))(implicit ctx: BasicContext): Future[X] = {
    import ctx.executionContext
    Future { xy._1 }
  }
}

case class TrivialProj2[-X, Y](
  compressionFactor: Double = java.lang.Math.nextDown(1.0d)
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

  def compute(secondArgument: B)(implicit ctx: BasicContext): Future[Z] = {
    import ctx.executionContext
    val aFut = firstArgument.evalAndGet
    val bFut = secondArgument.evalAndGet
    for {
      a <- aFut
      b <- bFut
      z <- f.compute((a, b))
    } yield z
  }

  private[scavenger] def transformSizeEstimates(before: Size, after: Size):
  (Size, Size) = {
    
  }
}


case class TrivialCurry2[-A, B, +Z](
  algorithm: TrivialAlgorithm[(A, B), Z],
  secondArgument: TrivialJob[B]
) extends TrivialAlgorithm[A, Z] {
  def apply(firstArgument: TrivialJob[A]): TrivialJob[Z] = 
    algorithm(TrivialPair(firstArgument, secondArgument))
}