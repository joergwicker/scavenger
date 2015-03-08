package scavenger.mains

import akka.actor._
import akka.actor.ActorSystem
import akka.actor.ActorPath
import com.typesafe.config.ConfigFactory
import scavenger.Context
import scavenger.backend.{Seed, Master, ActorContext, Worker}

/**
 * Provides a whole Scavenger service (seed, master, workers) on a 
 * single node.
 * 
 * This trait can be mixed in into a the object with 
 * the `main` function, for example in order to test the
 * behavior of the algorithm on a local machine before
 * submitting the job to the cluster.
 */
trait LocalScavengerApplication {

  private var system: ActorSystem = _
  private var master: ActorRef = _
  private var _context: Context = _
  
  def scavengerInit(numWorkers: Int = 16): Unit = {
    system = ActorSystem("scavenger")
    val seed = system.actorOf(Seed.props, "seed")
    val seedPath = seed.path
    master = system.actorOf(Master.props(seedPath), "master")
    _context = new ActorContext(
      master, 
      scala.concurrent.ExecutionContext.Implicits.global
    )
    for (i <- 1 to numWorkers) {
      system.actorOf(
        Worker.props(seedPath),
        scavenger.util.RandomNameGenerator.randomName
      )
    }
  }

  def context: Context = _context

  def scavengerShutdown(): Unit = {
    system.shutdown()
  }
}