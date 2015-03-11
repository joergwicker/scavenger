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
  def asExplicitResource[X](r: Resource[X]): Future[ExplicitResource[X]] = {
    for (x <- submit(r)) yield Value(r.identifier, x, CachingPolicy.Nowhere)
  }
}

//* <--- Add/remove the single / at the head of this line to block/unblock
// This is a little demo/usability test
object TrivialContext {
  import scala.concurrent.ExecutionContext.Implicits.global
  def main(args: Array[String]): Unit = {
    
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
    
    val twoParamFunc = cheap("twoParam") {
      (id: (Int, Double)) => {
        val (i,d) = id
        "foobar(" + (i - d) + ")"
      }
    }

    val currLeft: Algorithm[Double, String] = twoParamFunc.curryFst(x)
    val currRight: Algorithm[Int, String] = twoParamFunc.currySnd(y)

    for ( res <- ctx.submit(app1) ) println("Result 1 = " + res)
    for ( res <- ctx.submit(app2) ) println("Result 2 = " + res)
    for ( res <- ctx.submit(currLeft(y)) ) println("Result 3 = " + res)
    for ( res <- ctx.submit(currRight(x)) ) println("Result 4 = " + res)
  }
}
// <--- don't touch this. ---> */