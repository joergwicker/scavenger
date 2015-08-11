package scavenger.demo

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Try, Success, Failure}

import akka.util.Timeout

import scavenger._
import scavenger.app.DistributedScavengerApp


/** A little demo that shows how to get Scavenger up and running on
  * a single computer.
  *
  * This might be useful if you want to test it locally, before
  * installing the framework on the cluster.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
object Demo extends DistributedScavengerApp(){
  def main(args: Array[String]): Unit = {
    scavengerInit()

    val f0 = expensive("2pow"){ 
      (x: Int) => {
        Thread.sleep(50)
        math.pow(2, x).toInt % 3945
	
      }
    }
    val f1 = cheap("square"){ 
     (x: Int) => {
	     (x * x) % 5979 
     }
    }

    val f2 = expensive("times2"){
      Thread.sleep(90)
      (x: Int) => 2 * x
    }
    val f3 = parallel("adHocComputationExample"){
      (x: Int, ctx: Context) => {
        val adHocComputation = Computation(x).cacheGlobally
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
      g(f(Computation(inputId, d)).cacheGlobally)
    }


    val futures = for (j <- jobs) yield scavengerContext.submit(j)
    val allTogether = Future.sequence(futures)


   allTogether.onSuccess {

      case listOfResults: List[Int] => {
	
        println("#####Contents of the master cache: ")
        for (id <- scavengerContext.dumpCacheKeys) println(id)
        println("#####")


        for (entry <- listOfResults) println(entry)
        println("Sum = " + listOfResults.sum)
        scavengerShutdown()
      } 
    case _ => println("allTogether.onSuccess ")
    }

  }
}

