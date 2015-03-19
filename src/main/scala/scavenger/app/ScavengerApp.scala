package scavenger.app

import scavenger.Context

/** Interface of a mixin that should be added by users
  * to their application if they want to make use of
  * the Scavenger functionality in their `main`.
  *
  * Usable implementations are `LocalScavengerApp` and `DistributedScavengerApp`.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
private[app] trait ScavengerApp {
  
  /** Initializes an actor system and an actor that can handle user requests
    */
  def scavengerInit(): Unit
 
  /** Shutds down the underlying actor system
    */
  def scavengerShutdown(): Unit

  /** Returns a Scavenger computation context that can be used to submit requests to
    */
  def scavengerContext: Context

  /** Provides an implicit execution context
    */
  implicit def executionContext = scavengerContext.executionContext
  
}
