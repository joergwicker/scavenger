package hotswapWorker
import org.scalatest._
import akka.testkit.TestActorRef
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask

class WorkerSpec extends FlatSpec with Matchers {
  "Worker" should "be tested" in {
    info("info messages are better than printout")
    true should be (true)
  }

}