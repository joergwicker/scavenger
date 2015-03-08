package scavenger.backend

import akka.actor._
import scavenger.Resource

class Master(val seedPath: ActorPath) 
extends Actor 
with ActorLogging
with SeedJoin {

  def receive = ({
    case _ => ??? // TODO
  }: Receive)

}

object Master {

  def props(seedPath: ActorPath) = Props(classOf[Master], seedPath)

  /**
   * Handshake message sent to the seed node in the 
   * connection phase.
   */
  private[backend] case object MasterHere

  /**
   * A message used to delegate a job to the worker node.
   * This kind of communication is initiated by the master.
   */
  private[backend] case class Delegated(job: Resource[Any])

  /**
   * Message used to send some already computed results to the
   * worker node. 
   * This message is a response to worker's request to check 
   * whether a resource is already in master's cache.
   */
  private[backend] case class MasterResult(result: Resource[Any])
}