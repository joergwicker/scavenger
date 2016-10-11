package scavenger
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Mock-up of a `TrivialContext`, which pretends that a predefined
 * map with values 
 */
case class TrivialContextMockup(fakeCache: Map[Identifier, Value[_]]) 
extends TrivialContext {

  implicit def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
  private[scavenger] def loadFromCache[X](id: Identifier): Future[X] = {
    fakeCache(id).get(this).map{_.asInstanceOf[X]}
  }
  private[scavenger] def loadFromCache[X](selector: TrivialJob[X]): 
    Future[X] = {

    selector.evalAndGet(this)
  }
}