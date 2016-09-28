package scavenger

import scala.concurrent.{Future, ExecutionContext}
import scala.collection.generic.CanBuildFrom
import scala.collection.TraversableOnce
import scala.language.higherKinds
import scavenger.algebra.GCS

/**
 * `TrivialJob` requires only a `BasicContext` to compute its value,
 * and takes so little effort that it can be executed by workers, 
 * master, and even remote cache node.
 *
 * A backend implementation is allowed to run trivial jobs wherever it seems
 * fit in order to minimize the load on the network.
 * Where a `TrivialJob` is executed depends on the resulting load on
 * the network, not on the compute power of a node. If the result of a 
 * trivial job is expected to be smaller than the formal representation,
 * the result will be evaluated before the simplified formal representation
 * is sent over the network.
 *
 * @since 2.3
 * @author Andrey Tyukin
 */
sealed trait TrivialJob[+X] {
  
  def identifier: scavenger.Identifier

  /** 
   * Computes the result as `NewValue`
   */
  def eval(implicit ctx: BasicContext): Future[NewValue[X]]

  /** Computes and loads the result into memory of this JVM */
  def evalAndGet(implicit ctx: BasicContext): Future[X] = {
    for {
      v <- this.eval
      res <- v.get
    } yield res
  }

  import TrivialJob._

  /** Returns an equivalent job (which is compressed, if possible), 
    * together with exact size before compression, and an upper bound estimate
    * for the size after compression.
    */
  protected[scavenger] def _compressed(implicit ctx: BasicContext): 
    Future[(TrivialJob[X], Size, Size)]

  /** Returns an equivalent job which is maximally compressed for the 
    * serialization (that means: all trivial subjobs that decreased size of
    * the data have been evaluated).
    */
  private[scavenger] def compressed(implicit ctx: BasicContext): 
    Future[TrivialJob[X]] = {
      import ctx.executionContext
      for ((c, _, _) <- _compressed) yield c._1
    }
}

/** Application of a trivial algorithm to a trivial computation 
  * is again a trivial computation.
  */
case class TrivialApply[X, +Y](
  f: TrivialAlgorithm[X, Y], 
  x: TrivialJob[X]
) extends TrivialJob[Y] {
  def identifier = ??? // TODO
  def eval(implicit ctx: BasicContext): Future[NewValue[Y]] = {
    import ctx.executionContext
    for {
      xLoaded <- x.evalAndGet
      yResult <- f.compute(xLoaded)
    } yield InRam(yResult, this.identifier)
  }
  protected[scavenger] def _compressed(implicit ctx: BasicContext): 
  Future[(TrivialJob[Y], Size, Size)] = {
    import ctx.executionContext
    for {
      (xCompressed, xBefore, xAfter) <- x._compressed
      result <- f._compress(xCompressed, xBefore, xAfter)
    } yield result
  }
}

/** A pair of trivial computations is again trivial.
  */
case class TrivialPair[+X, +Y](
  _1: TrivialJob[X],
  _2: TrivialJob[Y]
) extends TrivialJob[(X, Y)] {
  def identifier = ??? // TODO
  def eval(implicit ctx: BasicContext): Future[NewValue[(X, Y)]] = {
    import ctx.executionContext
    val xFut = _1.eval
    val yFut = _2.eval
    for {
      xVal <- xFut
      yVal <- yFut
    } yield ValuePair(xVal, yVal)
  }
  protected[scavenger] def _compressed(implicit ctx: BasicContext):
  Future[(TrivialJob[(X, Y)], Size, Size)] = {
    
  }
}

case class TrivialJobs[+X, M[+E] <: TraversableOnce[E]](jobs: M[TrivialJob[X]])
(implicit 
  cbf1: CanBuildFrom[M[TrivialJob[X]], Future[NewValue[X]], M[Future[NewValue[X]]]],
  cbf2: CanBuildFrom[M[Future[NewValue[X]]], NewValue[X], M[NewValue[X]]],
  cbf3: CanBuildFrom[M[NewValue[X]], Future[X], M[Future[X]]],
  cbf4: CanBuildFrom[M[Future[X]], X, M[X]],
  cbf5: CanBuildFrom[M[TrivialJob[X]], TrivialJob[X], M[TrivialJob[X]]]
) extends TrivialJob[M[X]] {
  def identifier = ??? // TODO
  def eval(implicit ctx: BasicContext): Future[NewValue[M[X]]] = {
    import ctx.executionContext
    val futsBldr = cbf1(jobs)
    for (f <- jobs.map({_.eval(ctx)})) {
      futsBldr += f
    }
    val futs = futsBldr.result()
    val fut = Future.sequence(futs)(cbf2, ctx.executionContext)
    for (values <- fut) yield Values(values)(cbf3, cbf4)
  }

  import TrivialJob._
  protected def _compressed: Future[(TrivialJob[M[X]], Size, Size)] = {
    val compressedJobs = for (j <- jobs) yield j._compressed
    var maxBefore = GCS.zero[Identifier]
    var sumAfter = GCS.zero[Identifier]
    for ((_, b, a) <- compressedJobs) {
      maxBefore = maxBefore.max(b)
      sumAfter += a
    }
    val bldr = cbf5(jobs)
    for (j <- compressedJobs) bldr += j._1
    (TrivialJobs(bldr.result()), sumBefore, sumAfter)
  }
}

/** Wrapper for `Value`; requires no further evaluation. */
case class TrivialValue[+X](value: NewValue[X]) extends TrivialJob[X] {
  def identifier = value.identifier
  def eval(implicit ctx: BasicContext): Future[NewValue[X]] = {
    import ctx.executionContext
    Future { value }
  }
  protected[scavenger] def _compressed: Future[(TrivialJob[])]
  protected[scavenger] def _compressed(implicit ctx: BasicContext): 
  Future[(TrivialJob[X], Size, Size)] = {
    import ctx.executionContext
    val bv = GCS.basisVector(value.identifier)
    Future { (this, GCS.basisVector(value.identifier), G)}
  }
}

object TrivialJob {
  // private[TrivialJob] type TJ[+X] = TrivialJob[X]
  private[scavenger] type Size = algebra.GCS[scavenger.Identifier]
  
  /** Returns essentially the same (possibly optimized) computation, 
    * together with estimates for relative sizes before and after
    * explicit evaluation.
    */
  // Refer to [SE V p128] for derivation, notice that "before" and "after" are
  // swapped here.
  // private[TrivialJob] def optimizeForNetwork[X](t: T[X]): T[X] = {
  //   ???
  // }

  // private[TrivialJob] def optimizeForNetworkHelper[X](t: T[X]): 
  //   (T[X], Size, Size) = {
  //   ??? // TODO
  // }
}