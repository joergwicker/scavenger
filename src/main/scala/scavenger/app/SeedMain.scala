package scavenger.app

import akka.actor._
import com.typesafe.config.Config
import scavenger.backend.seed.Seed

/**
 * A `main` that starts a seed node.
 *
 * It initializes an actor system and spawns a the seed actor.
 * All arguments are ignored.
 */
object SeedMain extends ScavengerNode {

  def extractNodeConfig(generalConfig: Config): Config = {
    generalConfig.getConfig("seed") withFallback generalConfig
  }

  private[app] def initializeActors(
    system: ActorSystem, 
    generalConfig: Config
  ): Unit = {
    system.actorOf(Seed.props, "seed")
  }

  def main(args: Array[String]): Unit = scavengerInit()
}