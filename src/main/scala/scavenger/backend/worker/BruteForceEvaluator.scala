package scavenger.backend.worker

import scala.concurrent.Future
import scavenger.Resource
import scavenger.backend.ResourceEvaluator

trait BruteForceEvaluator extends ResourceEvaluator {
  def computeSimplified[X](r: Resource[X]): Future[X] = {
    throw new UnsupportedOperationException(
      "On worker nodes, all resources get scheduled as they are. " + 
      "Nothing gets simplified. Therefore, `computeSimplified` should " +
      "never be called."
    )
  }
}