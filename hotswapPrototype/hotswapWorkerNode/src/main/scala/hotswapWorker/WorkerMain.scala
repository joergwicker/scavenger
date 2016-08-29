package hotswapWorker
import akka.actor._

/*
object WorkerMain {
  def main(args: Array[String]): Unit = {
    println("Starting Worker actor system")
    val sys = ActorSystem("workersystem")
    val worker = sys.actorOf(Worker.props(5))
    worker ! "hello there!"
  }
}
*/