package scavenger.backend.worker

import scala.concurrent.Future
import scavenger.Computation
import scavenger.backend.SimpleComputationExecutor

trait BruteForceEvaluator extends SimpleComputationExecutor {
  def computeSimplified[X](r: Computation[X]): Future[X] = {
    throw new UnsupportedOperationException(
      "On worker nodes, all computations get scheduled as they are. " + 
      "Nothing gets simplified. Therefore, `computeSimplified` should " +
      "never be called."
    )
  }
}