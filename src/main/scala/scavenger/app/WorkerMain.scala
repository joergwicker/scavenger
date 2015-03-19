package scavenger.app

import akka.actor._
import com.typesafe.config.Config
import scavenger.backend.worker.Worker

/** A `main` that starts a worker node.
  *
  * It initializes an actor system and spawns a single worker.
  * All arguments are ignored.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
object WorkerMain extends ScavengerNode {

  def extractNodeConfig(generalConfig: Config): Config = {
    generalConfig.getConfig("worker") withFallback generalConfig
  }

  private[app] def initializeActors(
    system: ActorSystem, 
    generalConfig: Config
  ): Unit = {
    val seedPath = extractSeedPath(generalConfig)
    system.actorOf(
      Worker.props(seedPath),
      scavenger.util.RandomNameGenerator.randomName
    )
  }

  def main(args: Array[String]): Unit = scavengerInit()
}
