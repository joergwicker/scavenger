package scavenger.app

import akka.actor._
import com.typesafe.config.Config
import scavenger.backend.seed.Seed

/** A `main` that starts a seed node.
  *
  * It initializes an actor system and spawns a the seed actor.
  * All arguments are ignored.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
object SeedMain extends ScavengerNode {

  /** Extracts node-specific configuration from the general 
    * configuration.
    *
    * The user-modified configuration files are expected to
    * be structured as follows:
    * {{{
    * akka {
    *   // general akka settings
    * }
    * scavenger {
    *   seed {
    *     // seed-specific settings
    *   }
    *   master {
    *     // master-specific settings
    *   }
    *   //etc.
    * }
    * }}}
    * This method is responsible for extracting the right 
    * sub-configuration (seed, master, worker) from the
    * general configuration.
    */
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
