package scavenger.util

import scala.concurrent.{Future,ExecutionContext,Await}
import scala.concurrent.duration._
import scala.language.postfixOps
import scavenger._

/** Implementation of a trivial context that does nothing and
  * does not attempt to distribute or cache anything.
  *
  * Might be useful for testing purposes.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
class TrivialContext(printActions: Boolean) extends Context {
  implicit def executionContext = ExecutionContext.Implicits.global
  def submit[X](r: Computation[X]): Future[X] = {
    if (printActions) {
      println("Computing: " + r)
    }
    r.compute(this)
  }
  def asExplicitComputation[X](r: Computation[X]): 
    Future[ExplicitComputation[X]] = {
    for (x <- submit(r)) yield Value(r.identifier, x, CachingPolicy.Nowhere)
  }

  private[scavenger] def dumpCacheKeys = Nil
}

//* <--- Add/remove the single / at the head of this line to block/unblock
// This is a little demo/usability test
object TrivialContext {
  import scala.concurrent.ExecutionContext.Implicits.global
  def main(args: Array[String]): Unit = {
    
    val scavengerContext = new TrivialContext(true)

    val f0 = expensive("2pow"){ 
      (x: Int) => {
        Thread.sleep(200)
        math.pow(2, x).toInt % 3945
      }
    }
    val f1 = cheap("square"){ (x: Int) => (x * x) % 5979 }
    val f2 = expensive("times2"){
      Thread.sleep(300)
      (x: Int) => 2 * x
    }
    val f3 = parallel("cube"){
      (x: Int, ctx: Context) => {
        val adHocComputation = Computation(x)
        val subjob1 = f1(adHocComputation)
        val subjob2 = f2(adHocComputation)
        for {
          a <- ctx.submit(subjob1)
          b <- ctx.submit(subjob2)
        } yield (a * b / x % 87698)
      }
    }
    
    val data = List(5, 4)
    val functions = List(f0, f1, f2, f3)
    val jobs = for (d <- data; f <- functions; g <- functions) yield {
      val inputId = "number_" + d 
      g(f(Computation(inputId, d)))
    }

    val futures = for (j <- jobs) yield scavengerContext.submit(j)
    val allTogether = Future.sequence(futures)

    val listOfResults = Await.result(allTogether, 60 seconds) 
    for (entry <- listOfResults) println(entry)
    println("Sum = " + listOfResults.sum)

    /*
    val x = Computation("x", 5)
    val y = Computation("y", 7.0)
    val f = cheap("f"){
      (i: Int) => i * i
    }
    val g = parallel("g"){
      (d: Double, ctx: Context) => 
        Future(d * 3.0)(ctx.executionContext)
    }
    val classif = Computation[Int => Boolean](
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
    */
  }
}
// <--- don't touch this. ---> */
