package scavenger.demo

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
    scavengerInit()

    

    Thread.sleep(5)
    println("ok, enough...")
    scavengerShutdown()
  }
}