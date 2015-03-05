package scavenger.util

import scala.concurrent.{Future,ExecutionContext}
import scavenger._

/**
 * Implementation of a trivial context that does nothing and
 * does not attempt to distribute or cache anything
 */
class TrivialContext(printActions: Boolean) extends Context {
  implicit def executionContext = ExecutionContext.Implicits.global
  def submit[X](r: Resource[X]): Future[X] = {
    if (printActions) {
      println("Computing: " + r)
    }
    r.compute(this)
  }
}

/*
object TrivialContext {
  import scavenger._
  def main(args: Array[String]): Unit = {
    // a little demo/usability test
    val ctx = new TrivialContext(true)

    val x = Resource("x", 5)
    val y = Resource("y", 7.0)
    val f = cheap("f"){
      (i: Int) => i * i
    }
    val g = parallel("g"){
      (d: Double, ctx: Context) => 
        Future(d * 3.0)(ctx.executionContext)
    }
    val classif = Resource[Int => Boolean](
      "classif", 
      {(x: Int) => x > 0}
    )
    val app1 = classif(x, Cheap)
    val app2 = classif(f(x), Expensive)

    ctx.submit(app1)
    ctx.submit(app2)
  }
}
// */