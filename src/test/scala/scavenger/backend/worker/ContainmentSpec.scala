package scavenger.backend.worker
import org.scalatest._
import akka.testkit.TestActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import scavenger.backend.worker.protocol.worker_containment._
import scala.concurrent.Future
import scala.util._
import scala.concurrent.duration._
import scala.language.postfixOps

class ContainmentSpec
extends FlatSpec
with Matchers 
with MockupClient {
  
  implicit val actorSystem = ActorSystem("test_containment")
  implicit val timeout = Timeout(10 seconds)

  private val helloJob = """
    import scavenger.backend.worker.Job;
    import java.io.Serializable;

    public class prefix_Hello implements Job<String>, Serializable {
      public String doJob() {
        return "hello";
      }
    }
  """

  "Containment" should "load jobs and respond with `JobLoaded`" in {

    val c = TestActorRef(new Containment(
      List("prefix_"),
      List(tmpDirUrl)
    ))
    (receivingBytes("prefix_Hello", helloJob){
      bytes => 
      val response = c ? LoadJob(bytes)
      val Success(JobLoaded(_)) = response.value.get
    })
  }

  it should "report back list of loaded isolated classes" in {
    
  }

}