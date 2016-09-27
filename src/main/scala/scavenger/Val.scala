package scavenger 

import scala.collection.TraversableOnce
import scala.collection.generic.CanBuildFrom
import scala.concurrent.{Future, ExecutionContext}
import scala.language.higherKinds

/** Explicit value of type `X` that does not require any computation at all,
  * but might take some time to load from the remote cache.
  *
  * @since 2.3
  * @author Andrey Tyukin
  */
// TODO: tear down old `Value`, replace by new `Value`
sealed trait NewValue[+X] {
  def identifier: Identifier
  def get(ctx: BasicContext): Future[X]
}

case class InMemory[+X](value: X, identifier: Identifier) extends NewValue[X] {
  def get(ctx: BasicContext): Future[X] = {
    import ctx.executionContext
    Future { value }
  }
}

case class InRemoteCache[+X](identifier: Identifier) extends NewValue[X] {
  def get(ctx: BasicContext): Future[X] = ctx.loadFromGlobalCache(identifier)
}

case class ValuePair[+X, +Y](_1: NewValue[X], _2: NewValue[Y]) 
extends NewValue[(X, Y)] {
  def identifier = ??? // TODO
  def get(ctx: BasicContext): Future[(X, Y)] = {
    import ctx.executionContext
    val fx = _1.get(ctx)
    val fy = _2.get(ctx)
    for (vx <- fx; vy <- fy) yield (vx, vy)
  }
}

case class Values[+X, M[+E] <: TraversableOnce[E]]
  (values: M[NewValue[X]])
  (implicit cbf1: CanBuildFrom[M[NewValue[X]], Future[X], M[Future[X]]],
   cbf2: CanBuildFrom[M[Future[X]], X, M[X]]) 
extends NewValue[M[X]] {
  def identifier = ??? // TODO: need better CCC's now...
  def get(ctx: BasicContext): Future[M[X]] = {
    import ctx.executionContext
    val bldr1 = cbf1(values)
    for (v <- values) {
      bldr1 += v.get(ctx)
    }
    Future.sequence(bldr1.result())(cbf2, ctx.executionContext)
  }
}