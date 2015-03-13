package scavenger.backend

import akka.actor.{Actor, Props}
import scala.collection.mutable
import scala.collection.mutable.HashMap
import scala.concurrent.{Future, Promise}
import scavenger._
import scavenger.backend._
import scavenger.backend.LocalWorker._
import scavenger.categories.formalccc

/**
 * Common interface for a `LoadBalancer` (on Master node)
 * and a simple `BruteForceEvaluator` (on Worker node).
 *
 * It is able to evaluate "maximally simplified" `Computations`.
 * What "maximally simplified" means depends on the type of
 * node.
 */
trait SimpleComputationExecutor extends Actor with ContextProvider {

  import context.dispatcher

  private var internalLabelCounter: Long = 0L
  def toInternalLabel(identifier: formalccc.Elem): InternalLabel = {
    internalLabelCounter += 1
    InternalLabel(identifier, internalLabelCounter)
  }

  /**
   * Map of promised jobs.
   */
  protected val promises: mutable.Map[InternalLabel, Promise[Any]] =
    HashMap.empty[InternalLabel, Promise[Any]]

  protected def fulfillPromise(label: InternalLabel, result: Any): Unit = {
    println("DEBUG: fulfilling promise for id = " + label) // TODO: remove debug
    if (!promises.contains(label)) {
      println("ERROR: the promise for id = " + label + " does not exist")
      throw new Error("SimpleComputationExecutor.fulfillPromise: nothing to fulfill")
    } else if (promises(label).isCompleted) {
      println("ERROR: the promise for id = " + label + " is already completed!")
      throw new Error("SimpleComputationExecutor.fulfillPromise seems buggy")
    }
    promises(label).success(result)
    promises -= label
  }

  /**
   * Perform a simple computation that can be delegated.
   */
  def computeSimplified[X](r: Computation[X]): Future[X]

  /**
   * Perform a complex computation that can not be delegated.
   *
   * Simply spawns an little separate actor on same node, and
   * delegates the computation.
   */
  def computeHere[X](r: Computation[X]): Future[X] = {
    val spawned = context.actorOf(
      LocalWorker.props(provideComputationContext),
      "LOCAL_" + scavenger.util.RandomNameGenerator.randomName
    )
    val p = Promise[Any]
    val label = toInternalLabel(r.identifier)
    promises(label) = p
    spawned ! LocalJob(label, r)
    p.future.map{ 
      a => a.asInstanceOf[X] 
    }
  }

  def handleLocalResponses: Receive = ({
    case LocalResult(label, result) => {
      fulfillPromise(label, result)
    }
  }: Receive)
}
