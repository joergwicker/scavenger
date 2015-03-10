package scavenger.backend

import scala.actor.{Actor, Receive}
import scala.concurrent.{Future, Promise, ExecutionContext}

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
trait ExternalInterface extends Actor with Caching {
  def handleExternalRequests: Receive = ({
    case Compute(job, result) => {
      getComputed(job).onSuccess{ 
        r => result.success(r)
      }
    }
    case GetExplicitResource(job, result) => {
      getExplicit(job).onSuccess{
        r => result.success(r)
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