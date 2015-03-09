package scavenger.demo

import scala.concurrent.duration._
import scala.language.postfixOps
import scavenger._
import scavenger.mains.LocalScavengerApplication

/**
 * A little demo that shows how to get Scavenger up and running on 
 * a single computer.
 *
 * This might be useful if you want to test it locally, before 
 * installing the framework on the cluster.
 */
object LocalDemo extends LocalScavengerApplication {
  def main(args: Array[String]): Unit = {
    scavengerInit(2)

    val x = Resource("x", 5)
    val f = cheap("square"){ (x: Int) => x * x }
    val j = f(x)

    val fut = context.submit(j)

    implicit val execCtx = context.executionContext
    scheduler.scheduleOnce(30 seconds){ 
      scavengerShutdown()
    }
  }
}