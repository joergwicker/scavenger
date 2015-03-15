package scavenger.backend

import akka.actor.Actor
import scavenger.Context

/**
 * Mixin for `Actor`s that provide an `DemilitarizedZone`
 * that allows to create `Context`-decorators.
 */
trait ContextProvider extends Actor {
  def provideComputationContext: Context = new ReactiveContext(
    self,
    context.dispatcher
  )
}
