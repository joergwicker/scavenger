package scavenger.backend

import akka.actor.{Actor, Props}
import scala.collection.mutable
import scala.collection.mutable.HashMap
import scala.concurrent.{Future, Promise}
import scavenger._
import scavenger.backend._
import scavenger.categories.formalccc

/**
 * Common interface for a `LoadBalancer` (on Master node)
 * and a simple `BruteForceEvaluator` (on Worker node).
 *
 * It is able to evaluate "maximally simplified" `Resources`.
 * What "maximally simplified" means depends on the type of
 * node.
 */
trait ResourceEvaluator extends Actor with ActorContextProvider {

  import context.dispatcher

  /**
   * Map of promised jobs.
   */
  protected val promises: mutable.Map[formalccc.Elem, Promise[Any]] =
    HashMap.empty[formalccc.Elem, Promise[Any]]

  protected def fulfillPromise(id: formalccc.Elem, result: Any): Unit = {
    promises(id).success(result)
  }

  /**
   * Perform a simple computation that can be delegated.
   */
  def computeSimplified[X](r: Resource[X]): Future[X]

  /**
   * Perform a complex computation that can not be delegated.
   *
   * Simply spawns an little separate actor on same node, and
   * delegates the computation.
   */
  def computeHere[X](r: Resource[X]): Future[X] = {
    val spawned = context.actorOf(
      LocalWorker.props(provideComputationContext),
      "<LocalWorker>_" + scavenger.util.RandomNameGenerator.randomName
    )
    val p = Promise[Any]
    promises(r.identifier) = p
    spawned ! InternalJob(r)
    p.future.map{ a => a.asInstanceOf[X] }
  }
}