package scavenger.backend

import akka.actor.Actor
import scavenger.Context

/**
 * Mixin for `Actor`s that provide an `ExternalInterface`
 * that allows to create `Context`-decorators.
 */
trait ActorContextProvider extends Actor {
  def provideComputationContext: Context = new ActorContext(
    self,
    context.dispatcher
  )
}
