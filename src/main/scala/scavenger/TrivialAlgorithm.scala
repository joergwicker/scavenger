package scavenger 
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.immutable.Set
import scavenger.algebra.FutNat
import scavenger.util.Instance
import scavenger.TrivialJob.Size

sealed trait TrivialAlgorithm[-X, +Y] {

  /** Transforms a trivial job with parameter `X` into a trivial job with
    * parameter `Y`. The application is formal, no actual computation takes
    * place until `eval` is called on the resulting `TrivialJob`.
    */
  def apply(t: TrivialJob[X]): TrivialJob[Y] = TrivialApply(this, t)

  /** Performs the actual computation in the specified context. */
  def compute(x: X)(implicit ctx: TrivialContext): Future[Y]

  /** Returns a job that is equivalent to `this.apply(input)`, 
    * together with the exact size before compression, 
    * and an upper bound estimate for the size of 
    * `this.apply(input)` after compression. The returned job is 
    * fully evaluated if `after < before` holds. Otherwise, 
    * it can be partially compressed (i.e. some of the sub-jobs can
    * be replaced by their compressed versions).
    */
  // TODO CRUFT
  // protected[scavenger] def _compress(
  //   input: TrivialJob[X],
  //   before: Size, 
  //   after: Size
  // )(implicit ctx: TrivialContext): Future[(TrivialJob[Y], Size, Size)]

  protected[scavenger] def inputsInRam(s: Set[Instance]): Set[Instance]
  protected[scavenger] def fullEvalSize(s: Size): Size

  protected[scavenger] def mapChildren
    (nat: FutNat[TrivialContext, TrivialJob, TrivialJob])
    (implicit ctx: TrivialContext): 
    Future[TrivialAlgorithm[X, Y]]
}

abstract class TrivialAtomicAlgorithm[-X, +Y] extends TrivialAlgorithm[X, Y] {

  /** 
   * An upper bound for `size(x) / size(y)`, where `x` is some input of 
   * type `X`, `y` is the output of type `Y`.
   */
  def compressionFactor: Double

  // TODO CRUFT
  // protected[scavenger] def _compress(
  //   xCompressed: TrivialJob[X], 
  //   xBefore: Size, 
  //   xAfter: Size
  // )(implicit ctx: TrivialContext): Future[(TrivialJob[Y], Size, Size)] = {
  //   import ctx.executionContext
  //   val yAfter = xAfter * f.compressionFactor
  //   val partiallyEvaluated = TrivialApply(this, xCompressed)
  //   if (yAfter < xBefore) {
  //     for (fullyEvaluated <- partiallyCompressed) yield {
  //       (TrivialValue(fullyEvaluated), xBefore, yAfter)
  //     }
  //   } else {
  //     Future { (TrivialApply(xCompressed), xBefore, yAfter) }
  //   }
  // }

  protected[scavenger] def inputsInRam(s: Set[Instance]) = s
  protected[scavenger] def fullEvalSize(s: Size) = s * compressionFactor

  protected[scavenger] def mapChildren
    (nat: FutNat[TrivialContext, TrivialJob, TrivialJob])
    (implicit ctx: TrivialContext): 
    Future[TrivialAlgorithm[X, Y]] = {
    
    import ctx.executionContext
    Future { this }

  }

}

/** Do-nothing morphism.
  *
  * Sometimes useful in conjunction with `TrivialCouple`.
  * For example, `TrivialCouple(TrivialId, TrivialId)` takes one input, 
  * and duplicates it, so that the two copies can then be fed into two 
  * different algorithms.
  */
case class TrivialId[X]() extends TrivialAlgorithm[X, X] {
  
  def compute(x: X)(implicit ctx: TrivialContext): Future[X] = {
    import ctx.executionContext
    Future { x }
  }
  // TODO CRUFT
  // private[scavenger] def _compress(
  //   input: TrivialJob[X], 
  //   before: Size, 
  //   after: Size
  // )(implicit ctx: TrivialContext) = {
  //   import ctx.executionContext
  //   (input, before, after)
  // }
  protected[scavenger] def inputsInRam(s: Set[Instance]) = s
  protected[scavenger] def fullEvalSize(s: Size): Size = s

  protected[scavenger] def mapChildren
    (nat: FutNat[TrivialContext, TrivialJob, TrivialJob])
    (implicit ctx: TrivialContext): 
    Future[TrivialAlgorithm[X, X]] = {
    
    import ctx.executionContext
    Future { this }

  }
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
  def compute(x: X)(implicit ctx: TrivialContext): Future[Z] = {
    import ctx.executionContext
    for {
      yIntermedRes <- first.compute(x)
      zRes <- second.compute(yIntermedRes)
    } yield zRes
  }
  
  // TODO CRUFT
  // private[scavenger] def _compress(
  //   input: TrivialJob[X], 
  //   before: Size, 
  //   after: Size
  // )(implicit ctx: TrivialContext): Future[(TrivialJob[Z], Size, Size)] = {
  //   import ctx.executionContext
  //   for {
  //     (y, yb, ya) <- first._compress(input, before, after)
  //     result <- second._compress(y, yb, ya)
  //   } yield result
  // }

  protected[scavenger] def inputsInRam(s: Set[Instance]) = 
    second.inputsInRam(first.inputsInRam(s))
  protected[scavenger] def fullEvalSize(s: Size) = 
    second.fullEvalSize(first.fullEvalSize(s))

  protected[scavenger] def mapChildren
    (nat: FutNat[TrivialContext, TrivialJob, TrivialJob])
    (implicit ctx: TrivialContext): 
    Future[TrivialAlgorithm[X, Z]] = {
   
    import ctx.executionContext

    val fFut = first.mapChildren(nat) 
    val sFut = second.mapChildren(nat)
    for {
      fRes <- fFut
      sRes <- sFut
    } yield TrivialComposition(sRes, fRes)

  }
}

case class TrivialCouple[-X, +A, +B](
  left: TrivialAlgorithm[X, A],
  right: TrivialAlgorithm[X, B]
) extends TrivialAlgorithm[X, (A, B)] {
  def compute(x: X)(implicit ctx: TrivialContext): Future[(A, B)] = {
    import ctx.executionContext
    val lFut = left.compute(x)
    val rFut = right.compute(x)
    for {
      lRes <- lFut
      rRes <- rFut
    } yield (lRes, rRes)
  }

  protected[scavenger] def inputsInRam(s: Set[Instance]) = 
    left.inputsInRam(s) ++ right.inputsInRam(s)
  protected[scavenger] def fullEvalSize(s: Size) = {
    left.fullEvalSize(s) + right.fullEvalSize(s)
  }

  protected[scavenger] def mapChildren
    (nat: FutNat[TrivialContext, TrivialJob, TrivialJob])
    (implicit ctx: TrivialContext): 
    Future[TrivialAlgorithm[X, (A, B)]] = {
   
    import ctx.executionContext

    val lFut = left.mapChildren(nat) 
    val rFut = right.mapChildren(nat)
    for {
      lRes <- lFut
      rRes <- rFut
    } yield TrivialCouple(lRes, rRes)

  }
}

case class TrivialProj1[X, -Y](
  compressionFactor: Double = java.lang.Math.nextDown(1.0d)
) extends TrivialAtomicAlgorithm[(X, Y), X] {
  def compute(xy: (X, Y))(implicit ctx: TrivialContext): Future[X] = {
    import ctx.executionContext
    Future { xy._1 }
  }
}

case class TrivialProj2[-X, Y](
  compressionFactor: Double = java.lang.Math.nextDown(1.0d)
) extends TrivialAtomicAlgorithm[(X, Y), Y] {
  def compute(xy: (X, Y))(implicit ctx: TrivialContext): Future[Y] = {
    import ctx.executionContext
    Future { xy._2 }
  }
}

case class TrivialCurry1[A, -B, +Z](
  algorithm: TrivialAlgorithm[(A, B), Z],
  firstArgument: TrivialJob[A]
) extends TrivialAlgorithm[B, Z] {

  def compute(b: B)
    (implicit ctx: TrivialContext): Future[Z] = {

    import ctx.executionContext
    val aFut = firstArgument.evalAndGet
    for {
      a <- aFut
      z <- algorithm.compute((a, b))
    } yield z
  }

  protected[scavenger] def inputsInRam(s: Set[Instance]) =
    algorithm.inputsInRam(firstArgument.inputsInRam ++ s)
  protected[scavenger] def fullEvalSize(s: Size) = 
    algorithm.fullEvalSize(firstArgument.fullEvalSize + s)

  protected[scavenger] def mapChildren
    (nat: FutNat[TrivialContext, TrivialJob, TrivialJob])
    (implicit ctx: TrivialContext): 
    Future[TrivialAlgorithm[B, Z]] = {
    
    import ctx.executionContext

    val aFut = algorithm.mapChildren(nat)
    val fFut = nat(firstArgument)

    for {
      aRes <- aFut
      fRes <- fFut
    } yield TrivialCurry1(aRes, fRes)
  }
}

case class TrivialCurry2[-A, B, +Z](
  algorithm: TrivialAlgorithm[(A, B), Z],
  secondArgument: TrivialJob[B]
) extends TrivialAlgorithm[A, Z] {

  def compute(a: A)
    (implicit ctx: TrivialContext): Future[Z] = {

    import ctx.executionContext
    val bFut = secondArgument.evalAndGet
    for {
      b <- bFut
      z <- algorithm.compute((a, b))
    } yield z
  }

  protected[scavenger] def inputsInRam(s: Set[Instance]) = 
    algorithm.inputsInRam(s ++ secondArgument.inputsInRam)
  protected[scavenger] def fullEvalSize(s: Size) = 
    algorithm.fullEvalSize(s + secondArgument.fullEvalSize)

  protected[scavenger] def mapChildren
    (nat: FutNat[TrivialContext, TrivialJob, TrivialJob])
    (implicit ctx: TrivialContext): 
    Future[TrivialAlgorithm[A, Z]] = {
    
    import ctx.executionContext

    val aFut = algorithm.mapChildren(nat)
    val sFut = nat(secondArgument)

    for {
      aRes <- aFut
      sRes <- sFut
    } yield TrivialCurry2(aRes, sRes)
  }
}