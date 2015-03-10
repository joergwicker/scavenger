package scavenger.backend

import scala.concurrent.Future

/**
 * Common interface for a `LoadBalancer` (on Master node)
 * and a simple `BruteForceEvaluator` (on Worker node).
 *
 * It is able to evaluate "maximally simplified" `Resources`.
 * What "maximally simplified" means depends on the type of
 * node.
 */
trait ResourceEvaluator {
  /**
   * Perform a simple computation that can be delegated.
   */
  def computeSimple(r: Resource[X]): Future[X]

  /**
   * Perform a complex computation that can not be delegated.
   */
  def computeHere(r: Resource[X]): Future[X]
}