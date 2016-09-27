package scavenger

import scala.concurrent.{Future, ExecutionContext}



/**
 *
 */
sealed trait TrivialComputation[+X] {
  def optimizeForNetwork: TrivialComputation[X] = TrivialComputation._optimizeForNetwork(this)._1
  def identifier: scavenger.Identifier
  def compute(ctx: BasicContext): TrivialComputation[X]
}





case class TrivialApply[X, +Y](
  f: TrivialAtomicAlgorithm[X, Y], 
  x: TrivialComputation[X]
) {
}

object TrivialComputation {
  private type T[+X] = TrivialComputation[X]
  private type RelativeSize = algebra.GCS[scavenger.Identifier]
  
  /**
   * Returns essentially the same (possibly optimized) computation, 
   * together with estimates for relative sizes before and after
   * explicit evaluation.
   * 
   * 
   * 
   */
  // Refer to [SE V p128] for derivation, notice that "before" and "after" are
  // swapped here.
  private[TrivialComputation] def _optimizeForNetwork[X](t: T[X]): 
    (T[X], RelativeSize, RelativeSize) = {
    ??? // TODO
  }
}