package scavenger.mains

import akka.actor._
import akka.actor.ActorSystem
import akka.actor.ActorPath
import com.typesafe.config.ConfigFactory
import scavenger.backend

class Blup(name: String) extends Actor {
  def receive = ({
    case x => println("Yuhu, %s got ".format(name) + x)
  }: Receive)
}

object IsolatedActorSystemStartTest {
  def main(args: Array[String]): Unit = {
    // val config = ConfigFactory.load()
    println("Starting system...")
    val system = ActorSystem("scavenger")
    println("Create foo:")
    val foo = system.actorOf(Props(classOf[Blup], "Barack"), "foo")
    println("send stuff to foo")
    foo ! "Greetings to Freeman"
  }
}