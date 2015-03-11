package scavenger.backend

import akka.actor.Actor
import scala.concurrent.{Future, Promise, ExecutionContext}
import scavenger._

/**
 * This is the second component of a `Context` implementation on 
 * a compute node. It gets messages containing `Promise`s from
 * an `ActorContext`, and makes sure that these promises are
 * not passed any further. In some sense, it provides kind of
 * demilitarized zone between the `Future`-computing model and
 * the actor-model.
 *
 * Empty `Promise`s shall not pass!
 */
trait ExternalInterface extends Actor with Cache {
 
  import ExternalInterface._
  import context.dispatcher 
  
  def handleExternalRequests: Receive = ({
    case Compute(job, result) => {
      getComputed(job).onSuccess{ 
        case r: Any => result.success(r)
      }
    }
    case GetExplicitResource(job, result) => {
      getExplicit(job).onSuccess{
        case r: ExplicitResource[Any] => result.success(r)
      }
    }
  }: Receive)
}

object ExternalInterface {

  /**
   * First (1/2) type of messages accepted by an `ExternalInterface`.
   * Requests the evaluation of `job`. The result should be written into 
   * the `result`-`Promise`.
   */
  private[backend] case class Compute(job: Resource[Any], result: Promise[Any])

  /**
   * Second (2/2) type of messages accepted by an `ExternalInterface`.
   * Similar to `Compute`, but does not need the final value, accepts a 
   * slightly more general `ExplicitResource` instead.
   */
  private[backend] case class GetExplicitResource(
    job: Resource[Any], 
    result: Promise[ExplicitResource[Any]]
  )
}
