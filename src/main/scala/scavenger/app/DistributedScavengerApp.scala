package scavenger.app

import akka.actor._
import akka.actor.ActorSystem
import akka.actor.ActorPath
import com.typesafe.config.Config
import scavenger.Context
import scavenger.backend._
import scavenger.backend.master.Master
import scavenger.backend.worker.Worker

/**
 * Base class for an application that wants to make use of the 
 * distributed Scavenger service.
 */
abstract class DistributedScavengerApp
extends ScavengerApp
with ScavengerNode {

  private var context: Option[Context] = None
  
  def extractNodeConfig(generalConfig: Config): Config = {
    generalConfig.getConfig("master") withFallback generalConfig
  }

  private[app] def initializeActors(
    system: ActorSystem, 
    generalConfig: Config
  ): Unit = {

    val seedPath = extractSeedPath(generalConfig)
    val master = system.actorOf(Master.props(seedPath), "master")

    context = Some(new ReactiveContext(
      master,
      scala.concurrent.ExecutionContext.Implicits.global
    ))
  }

  def scavengerContext = context.getOrElse {
    throw new IllegalStateException(
      "Attempted to use `scavengerContext` on a not " +
      "initialized scavenger node. Please call `scavengerInit()` at the " +
      "start of the application."
    )
  }
}