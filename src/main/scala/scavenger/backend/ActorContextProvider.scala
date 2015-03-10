package scavenger.backend

import scavenger.Context

/**
 * Mixin for `Actor`s that provide an `ExternalInterface`
 * that allows to create `Context`-decorators.
 */
trait ActorContextProvider extends Actor with ExternalInterface {
  def getContext: Context = new ActorContext(
    this,
    context.dispatcher
  )
}