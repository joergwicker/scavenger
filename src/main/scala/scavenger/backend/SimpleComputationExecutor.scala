package scavenger.backend

import akka.actor.{Actor, Props, ActorLogging}
import scala.collection.mutable
import scala.collection.mutable.HashMap
import scala.concurrent.{Future, Promise}
import scavenger._
import scavenger.backend._
import scavenger.backend.LocalWorker._
import scavenger.categories.formalccc

/** Common interface for a `LoadBalancer` (on Master node)
  * and a simple `BruteForceEvaluator` (on Worker node).
  *
  * It is able to evaluate "maximally simplified" `Computations`.
  * What "maximally simplified" means depends on the type of
  * node.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
trait SimpleComputationExecutor 
extends Actor 
with ActorLogging
with ContextProvider {

  import context.dispatcher

  private var internalLabelCounter: Long = 0L
  
  /** Wraps an identifier into an `InternalLabel`, adds additional
    * `Long` id to avoid collisions.
    */
  def toInternalLabel(identifier: formalccc.Elem): InternalLabel = {
    internalLabelCounter += 1
    InternalLabel(identifier, internalLabelCounter)
  }

  /** Map of promised jobs.
    */
  protected val promises: mutable.Map[InternalLabel, Promise[Any]] =
    HashMap.empty[InternalLabel, Promise[Any]]

  // just additional debug information...
  // Where do we lose stuff?
  private var allFulfilledPromises: List[InternalLabel] = Nil

  /** Helper method for fulfilling promises */
  protected def fulfillPromise(label: InternalLabel, result: Any): Unit = {
    log.debug("fulfilling promise for id = " + label)
    if (!promises.contains(label)) {
      println("ERROR: the promise for id = " + label + " does not exist")
      throw new Error("SimpleComputationExecutor.fulfillPromise: nothing to fulfill")
    } else if (promises(label).isCompleted) {
      println("ERROR: the promise for id = " + label + " is already completed!")
      throw new Error("SimpleComputationExecutor.fulfillPromise seems buggy")
    }
    allFulfilledPromises ::= label
    promises(label).success(result)
    promises -= label
    log.debug("All promises fulfilled so far: \n " + 
      allFulfilledPromises.mkString("\n"))
  }

  /** Perform a simple computation that can be delegated.
    */
  def computeSimplified[X](r: Computation[X]): Future[X]

  /** Perform a complex computation that can not be delegated.
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
