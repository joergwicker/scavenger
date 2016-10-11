package scavenger
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Frees us from importing all the weird `duration`-sub-sub-packages 
 * and shutting off the warnings from scala.language.postfixOps
 */
trait SimpleAwait {
  def waitALittle[X](f: Future[X]): X = Await.result(f, 3 seconds)
  def waitAMinute[X](f: Future[X]): X = Await.result(f, 60 seconds)
  def waitAnHour[X](f: Future[X]): X = Await.result(f, 1 hours)
  def waitForever[X](f: Future[X]): X = Await.result(f, 1000 hours)
}