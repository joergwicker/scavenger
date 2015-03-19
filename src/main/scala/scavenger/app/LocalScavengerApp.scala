package scavenger.app

import akka.actor._
import akka.actor.ActorSystem
import akka.actor.ActorPath
import com.typesafe.config.Config
import scavenger.Context
import scavenger.backend.ReactiveContext
import scavenger.backend.seed.Seed
import scavenger.backend.master.Master
import scavenger.backend.worker.Worker

/** Implements the complete Scavenger service (seed, master, workers) on a
  * single physical computer.
  *
  * This trait can be mixed in into the object with
  * the `main` function, for example in order to test the
  * behavior of the algorithm on a local machine before
  * submitting the job to the cluster.
  *
  * @constructor initializes an actor system with all required node types an a single JVM
  * @param numWorkers number of virtual worker nodes created locally
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
abstract class LocalScavengerApp(val numWorkers: Int) 
extends ScavengerApp
with ScavengerNode {

  private var context: Option[Context] = None
  
  /** Don't use any node-specific config, it doesn't make sense here.
    */
  private[app] def extractNodeConfig(generalConfig: Config): Config = generalConfig

  private[app] def initializeActors(
    system: ActorSystem, 
    generalConfig: Config
  ): Unit = {
    val seed = system.actorOf(Seed.props, "seed")
    val seedPath = seed.path
    val master = system.actorOf(Master.props(seedPath), "master")

    context = Some(new ReactiveContext(
      master,
      scala.concurrent.ExecutionContext.Implicits.global
    ))

    for (i <- 1 to numWorkers) {
      system.actorOf(
        Worker.props(seedPath),
        scavenger.util.RandomNameGenerator.randomName
      )
    }
  }

  def scavengerContext = context.getOrElse {
    throw new IllegalStateException(
      "Attempted to use `scavengerContext` on a not " +
      "initialized scavenger node. Please call `scavengerInit()` at the " +
      "start of the application."
    )
  }
}
