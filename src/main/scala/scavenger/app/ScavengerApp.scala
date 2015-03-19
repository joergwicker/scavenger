package scavenger.app

import scavenger.Context

/**
 * Interface of a mixin that should be added by users
 * to their application if they want to make use of
 * the Scavenger functionality in their `main`.
 *
 * Usable implementations are `LocalScavengerApp` and `DistributedScavengerApp`.
 */
private[app] trait ScavengerApp {
  
  def scavengerInit(): Unit
  def scavengerShutdown(): Unit

  def scavengerContext: Context
  implicit def executionContext = scavengerContext.executionContext
  
}