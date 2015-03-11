package scavenger

import scavenger._
import scavenger.categories.formalccc

/**
 * This package contains an implementation of a
 * Scavenger-service based on Akka.
 * 
 * It contains implementation of Master, Worker and Seed
 * actors that exchange messages in order to perform the
 * computation in distributed fashion.
 */
package object backend {
  trait HandshakeMessage

  /**
   * Internal jobs that are sent from the Master node to the Worker nodes
   */
  private[backend] case class InternalJob(job: Resource[Any])
  
  /**
   * Results sent from Workers to Master
   */
  private[backend] case class InternalResult(id: formalccc.Elem, result: Any)

  /**
   * Message that tells the worker that there is currently nothing to do.
   */
  private[backend] case object NoJobsAvailable
  
  /**
   * Message that is broadcast to all workers when there are new 
   * jobs available
   */
  private[backend] case object JobsAvailable
}