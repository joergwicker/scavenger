package scavenger

import scala.concurrent.{Future, ExecutionContext}
import scala.collection.generic.CanBuildFrom
import scala.collection.Set
import scala.collection.TraversableOnce
import scala.language.higherKinds
import scavenger.algebra.GCS
import scavenger.algebra.FutNat
import scavenger.util.Instance

/** `TrivialJob` requires only a `TrivialContext` to compute its value,
  * and takes so little effort that it can be executed by workers, 
  * master, and even remote cache node.
  *
  * A backend implementation is allowed to run trivial jobs wherever it seems
  * fit in order to minimize the load on the network.
  * Where a `TrivialJob` is executed depends on the resulting load on
  * the network, not on the compute power of a node. If the result of a 
  * trivial job is expected to be smaller than the unevaluated representation,
  * the result will be evaluated before the simplified formal representation
  * is sent over the network.
  *
  * @since 2.3
  * @author Andrey Tyukin
  */
sealed trait TrivialJob[+X] {
  
  def identifier: scavenger.Identifier

  /** Computes the result as `NewValue`
    */
  def eval(implicit ctx: TrivialContext): Future[NewValue[X]]

  /** Computes and loads the result into memory of this JVM */
  def evalAndGet(implicit ctx: TrivialContext): Future[X] = {
    import ctx.executionContext
    for (v <- this.evalAndGet) yield v
  }

  import TrivialJob._

  /** Enumerates all instances that would have to be serialized and 
    * sent over the network, if we attempted to send this job as-is.
    */
  protected[scavenger] def inputsInRam: Set[Instance]

  /** Computes an estimate of the size of the fully evaluated job. */
  protected[scavenger] def fullEvalSize: GCS[Instance]

  /** Applies `f` to all children, and then rebuilds a
    * formal job description of same shape.
    *
    * Sort of "Asynchronous context-sensitive catamorphism".
    */
  /* [TODO: temporary deactivated until everything else compiles]
  protected[scavenger] def mapChildren
    (f: FutNat[TrivialContext, TrivialJob, TrivialJob])
    (implicit ctx: TrivialContext):
    Future[TrivialJob[X]]
  */

  /** Returns an equivalent job which is maximally compressed for the 
    * serialization (that means: all trivial subjobs that could be 
    * compressed by evaluation, have been evaluated).
    */
  /*
  private[scavenger] def compressed(implicit ctx: TrivialContext): 
  Future[TrivialJob[X]] = {
    if (fullEvalSize < GCS.charFct(inputsInRam)) {
      eval
    } else {
      mapChildren(_.compressed)
    }
  }
  */
}

/** Application of a trivial algorithm to a trivial computation 
  * is again a trivial computation.
  */
case class TrivialApply[X, +Y](
  f: TrivialAlgorithm[X, Y], 
  x: TrivialJob[X]
) extends TrivialJob[Y] {
  def identifier = ??? // TODO
  def eval(implicit ctx: TrivialContext): Future[NewValue[Y]] = {
    import ctx.executionContext
    for {
      xLoaded <- x.evalAndGet
      yResult <- f.compute(xLoaded)
    } yield InRam(yResult, this.identifier)
  }

  // TODO CRUFT
  // protected[scavenger] def _compressed(implicit ctx: TrivialContext): 
  // Future[(TrivialJob[Y], Size, Size)] = {
  //   import ctx.executionContext
  //   for {
  //     (xCompressed, xBefore, xAfter) <- x._compressed
  //     result <- f._compress(xCompressed, xBefore, xAfter)
  //   } yield result
  // }

  protected[scavenger] lazy val inputsInRam: Set[Instance] = {
    f.inputsInRam(x.inputsInRam)
  }
  protected[scavenger] lazy val fullEvalSize: GCS[Instance] = {
    f.fullEvalSize(x.fullEvalSize)
  }
  
  /*
  protected[scavenger] def mapChildren
    (nat: FutNat[TrivialContext, TrivialJob, TrivialJob])
    (implicit ctx: TrivialContext):
    Future[TrivialJob[Y]] = {

    import ctx.executionContext

    for {
      mappedX <- nat(x)
      mappedF <- f.mapChildren(nat)
    } yield TrivialApply(mappedF, mappedX)

  }
  */

}

/** A pair of trivial computations is again trivial.
  */
case class TrivialPair[+X, +Y](
  _1: TrivialJob[X],
  _2: TrivialJob[Y]
) extends TrivialJob[(X, Y)] {
  def identifier = ??? // TODO
  def eval(implicit ctx: TrivialContext): Future[NewValue[(X, Y)]] = {
    import ctx.executionContext
    val xFut = _1.eval
    val yFut = _2.eval
    for {
      xVal <- xFut
      yVal <- yFut
    } yield ValuePair(xVal, yVal)
  }
  
  protected[scavenger] lazy val inputsInRam: Set[Instance] = 
    _1.inputsInRam ++ _2.inputsInRam
  protected[scavenger] lazy val fullEvalSize: GCS[Instance] = 
    _1.fullEvalSize + _2.fullEvalSize
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
  def eval(implicit ctx: TrivialContext): Future[NewValue[M[X]]] = {
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
  // CRUFT TODO
  // protected def _compressed: Future[(TrivialJob[M[X]], Size, Size)] = {
  //   val compressedJobs = for (j <- jobs) yield j._compressed
  //   var maxBefore = GCS.zero[Identifier]
  //   var sumAfter = GCS.zero[Identifier]
  //   for ((_, b, a) <- compressedJobs) {
  //     maxBefore = maxBefore.max(b)
  //     sumAfter += a
  //   }
  //   val bldr = cbf5(jobs)
  //   for (j <- compressedJobs) bldr += j._1
  //   (TrivialJobs(bldr.result()), sumBefore, sumAfter)
  // }

  protected[scavenger] lazy val inputsInRam: Set[Instance] = {
    jobs.map(_.inputsInRam).foldLeft(Set.empty[Instance])(_ ++ _)
  }

  protected[scavenger] lazy val fullEvalSize: GCS[Instance] = {
    jobs.map(_.fullEvalSize).foldLeft(GCS.zero[Instance])(_ + _)
  }
}

/** Wrapper for `Value`; requires no further evaluation. */
case class TrivialValue[+X](value: NewValue[X]) extends TrivialJob[X] {
  def identifier = value.identifier
  def eval(implicit ctx: TrivialContext): Future[NewValue[X]] = {
    import ctx.executionContext
    Future { value }
  }

  // TODO CRUFT
  // protected[scavenger] def _compressed: Future[(TrivialJob[])]
  // protected[scavenger] def _compressed(implicit ctx: TrivialContext): 
  // Future[(TrivialJob[X], Size, Size)] = {
  //   import ctx.executionContext
  //   val bv = GCS.basisVector(value.identifier)
  //   Future { (this, GCS.basisVector(value.identifier), G)}
  // }

  protected[scavenger] lazy val inputsInRam: Set[Instance] = value.inputsInRam
  protected[scavenger] lazy val fullEvalSize: GCS[Instance] = value.fullEvalSize
}

/** A trivial job that is delegated to the remote cache node.
  * 
  * The job is expected to be "compressing" in the sense that
  * the result of the computation is smaller than 
  * the inputs loaded from the cache. 
  * For example, retrieving a value from the cache and 
  * applying a projection to it, would be a typical selector-job,
  * which should be executed on the cache node.
  */
/* CRUFT: deemed unnecessary 2016-10-10; subjobs of this kind will be extracted
 * on-demand.
 *
case class TrivialSelectorJob[+X](selector: TrivialJob[X])
extends TrivialJob[X] {

  def eval(implicit ctx: TrivialContext): Future[NewValue[X]] = {
    ctx.loadFromCache(selector)
  }
}
*/

object TrivialJob {
  private[scavenger] type Size = algebra.GCS[Instance]
  
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