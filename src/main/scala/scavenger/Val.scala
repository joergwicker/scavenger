package scavenger 

import scala.collection.TraversableOnce
import scala.collection.generic.CanBuildFrom
import scala.concurrent.{Future, ExecutionContext}
import scala.language.higherKinds
import scavenger.util.Instance
import scavenger.algebra.GCS

/** Explicit value of type `X` that does not require any computation at all,
  * but might take some time to load from the remote cache.
  *
  * @since 2.3
  * @author Andrey Tyukin
  */
// TODO: tear down old `Value`, replace by new `Value`
sealed trait NewValue[+X] {
  def identifier: Identifier
  def get(implicit ctx: BasicContext): Future[X]
  protected[scavenger] def inputsInRam: Set[Instance]
  protected[scavenger] def fullEvalSize: GCS[Instance]
}

/** A value that is available in the memory on this JVM.
  */
private[scavenger] case class InRam[+X](value: X, identifier: Identifier) 
extends NewValue[X] {
  def get(implicit ctx: BasicContext): Future[X] = {
    import ctx.executionContext
    Future { value }
  }
  protected[scavenger] def inputsInRam = Set(Instance(this))
  protected[scavenger] def fullEvalSize = GCS.basisVector(Instance(this))
}

/** A value that can be easily retrieved from a (remote) cache.
  */
private[scavenger] case class InCache[+X](identifier: Identifier) 
extends NewValue[X] {
  def get(implicit ctx: BasicContext): Future[X] = 
    ctx.loadFromCache(identifier)

  /* This means: identifiers are tiny and lightweight, and occupy essentially
   * no space.
   */
  protected[scavenger] def inputsInRam = Set.empty

  /* This captures the fact that loading values from remote cache 
   * can blow up their size by an arbitrarily large factor: we go from
   * a tiny identifier to a potentially large data structure.
   */
  protected[scavenger] def fullEvalSize = 
    GCS.basisVector(Instance(this)) * Double.PositiveInfinity
}

/** Pair of values, where both values can be scattered across multiple nodes.
  */
case class ValuePair[+X, +Y](_1: NewValue[X], _2: NewValue[Y]) 
extends NewValue[(X, Y)] {
  def identifier = ??? // TODO
  def get(implicit ctx: BasicContext): Future[(X, Y)] = {
    import ctx.executionContext
    val fx = _1.get(ctx)
    val fy = _2.get(ctx)
    for (vx <- fx; vy <- fy) yield (vx, vy)
  }
  def inputsInRam = _1.inputsInRam ++ _2.inputsInRam
  protected[scavenger] def fullEvalSize = _1.fullEvalSize + _2.fullEvalSize
}

/** A collection of fully evaluated values, which can be scattered across
  * multiple nodes.
  */
case class Values[+X, M[+E] <: TraversableOnce[E]]
  (values: M[NewValue[X]])
  (implicit cbf1: CanBuildFrom[M[NewValue[X]], Future[X], M[Future[X]]],
   cbf2: CanBuildFrom[M[Future[X]], X, M[X]]) 
extends NewValue[M[X]] {
  def identifier = ??? // TODO: need better CCC's now...
  def get(implicit ctx: BasicContext): Future[M[X]] = {
    import ctx.executionContext
    val bldr1 = cbf1(values)
    for (v <- values) {
      bldr1 += v.get(ctx)
    }
    Future.sequence(bldr1.result())(cbf2, ctx.executionContext)
  }
  protected[scavenger] def inputsInRam = 
    values.map(_.inputsInRam).foldLeft(Set.empty){_ ++ _}
  protected[scavenger] def fullEvalSize = 
    values.map(_.fullEvalSize).foldLeft(GCS.zero)(_ + _)
}