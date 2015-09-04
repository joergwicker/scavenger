package scavenger.backend

import akka.actor.{Actor, ActorLogging}
import scala.concurrent.{Future, Promise, ExecutionContext}
import scavenger._

/** Layer of `Master` and `Worker` nodes that prevents empty `Promise`s from
  * a `ReactiveContext` from spreading across the rest of the actor system.
  * 
  * This is the second component of a `Context` implementation on
  * a compute node. It gets messages containing `Promise`s from
  * an `ReactiveContext`, and makes sure that these promises are
  * not passed any further. In some sense, it provides kind of
  * demilitarized zone between the method-calls-with-`Future`-computing model and
  * the actor-model.
  *
  * Empty `Promise`s shall not pass!
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
trait DemilitarizedZone extends Actor with ActorLogging with Cache {
 
  import DemilitarizedZone._
  import context.dispatcher 
  
  def handleExternalRequests: Receive = ({
    case Compute(job, result) => {
      log.debug("DMZ: got Compute {}", job)
      getComputed(job).onSuccess{
        case r: Any => result.success(r)
      }
    }
    case GetExplicitComputation(job, result) => {
      log.debug("DMZ: got GetExplicitComputation {}", job)
      getExplicit(job).onSuccess{
        case r: ExplicitComputation[Any] => result.success(r)
      }
    }
  }: Receive)
}

/**
 * Provides message types used by `ReactiveContext` to translate 
 * method calls into messages 
 */
object DemilitarizedZone {

  /** First (1/2) type of messages accepted by an `DemilitarizedZone`.
    * Requests the evaluation of `job`. The result should be written into
    * the `result`-`Promise`.
    */
  private[backend] case class Compute(
    job: Computation[Any], 
    result: Promise[Any]
  )

  /** Second (2/2) type of messages accepted by an `DemilitarizedZone`.
    * Similar to `Compute`, but does not need the final value, accepts a
    * slightly more general `ExplicitComputation` instead.
    */
  private[backend] case class GetExplicitComputation(
    job: Computation[Any], 
    result: Promise[ExplicitComputation[Any]]
  )
}
