package scavenger
import org.scalatest._
import scala.concurrent.Future

class TrivialJobSpec extends FlatSpec with Matchers with SimpleAwait {

  /**
   * Frequently used example:
   *  trivial algorithm that replicates a string multiple times.
   */
  val repeatString = new TrivialAtomicAlgorithm[(String, Int), String] {
    def compute(x: (String, Int))(implicit ctx: TrivialContext): 
      Future[String] = {

      val (str, num) = x

      import ctx.executionContext
      Future { str * num }
    }
    def compressionFactor: Double = Double.PositiveInfinity
  }

  /*
  "TrivialContextMockup" should 
  "enable the test framework to evaluate simple examples (1)" in {
    val ctx = TrivialContextMockup(Map.empty)
    val tI = TrivialValue(Value(5))
    tI.evalAndGet(ctx)
  }

  "TrivialJob" should "be composable from simple parts (should compile)" in {
    val tI = TrivialValue(Value(2))
    val tS = TrivialValue(Value("test"))
    val job = repeatString(tS zip tI)
  }

  it should "evatuate in TrivialContext (very simple)" in {
    val tI = TrivialValue(Value(2))
    val tS = TrivialValue(Value("test"))
    val job = repeatString(tS zip tI)
    

  }
  */
}