package scavenger

import scala.concurrent.{Future, ExecutionContext}

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
  
  private[scavenger] def optimizeForNetwork: TrivialJob[X] 
    = TrivialJob.optimizeForNetwork(this)
  def identifier: scavenger.Identifier

  /** 
   * Computes the result 
   */
  def eval(implicit ctx: BasicContext): Future[NewValue[X]]
}

/** Application of a trivial algorithm to a trivial computation 
  * is again a trivial computation.
  */
case class TrivialApply[X, +Y](
  f: TrivialAtomicAlgorithm[X, Y], 
  x: TrivialJob[X]
) extends TrivialJob[Y] {
  def identifier = ??? // TODO
  def eval(implicit ctx: BasicContext): Future[NewValue[Y]] = {
    import ctx.executionContext
    for {
      xValue <- x.eval
      xLoaded <- xValue.get
      yResult <- f.compute(xLoaded)
    } yield InRam(yResult, this.identifier)
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
}

object TrivialJob {
  private type T[+X] = TrivialJob[X]
  private type RelativeSize = algebra.GCS[scavenger.Identifier]
  
  /** Returns essentially the same (possibly optimized) computation, 
    * together with estimates for relative sizes before and after
    * explicit evaluation.
    */
  // Refer to [SE V p128] for derivation, notice that "before" and "after" are
  // swapped here.
  private[TrivialJob] def optimizeForNetwork[X](t: T[X]): T[X] = {
    ???
  }

  private[TrivialJob] def optimizeForNetworkHelper[X](t: T[X]): 
    (T[X], RelativeSize, RelativeSize) = {
    ??? // TODO
  }
}