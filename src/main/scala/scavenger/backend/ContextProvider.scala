package scavenger.backend

import akka.actor.Actor
import scavenger.Context

/** Mixin for `Actor`s that provide an `DemilitarizedZone`
  * that allows to create `Context`-decorators.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
trait ContextProvider extends Actor {
  /** Returns a Scavenger computation context backed by this actor */
  def provideComputationContext: Context = new ReactiveContext(
    self,
    context.dispatcher
  )
}
