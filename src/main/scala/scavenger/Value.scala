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
sealed trait Value[+X] {
  def identifier: Identifier
  def get(implicit ctx: TrivialContext): Future[X]
  protected[scavenger] def inputsInRam: Set[Instance]
  protected[scavenger] def fullEvalSize: GCS[Instance]
}

object Value {
  def apply[X](x: X, id: Identifier): Value[X] = InRam(x, id)
}

/** A value that is available in the memory on this JVM.
  */
private[scavenger] case class InRam[+X](value: X, identifier: Identifier) 
extends Value[X] {
  def get(implicit ctx: TrivialContext): Future[X] = {
    import ctx.executionContext
    Future { value }
  }
  protected[scavenger] def inputsInRam = Set(new Instance(this))
  protected[scavenger] def fullEvalSize = GCS.basisVector(new Instance(this))
}

/** A value that can be easily retrieved from a (remote) cache.
  */
private[scavenger] case class InCache[+X](identifier: Identifier) 
extends Value[X] {
  def get(implicit ctx: TrivialContext): Future[X] = 
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
    GCS.basisVector(new Instance(this)) * Double.PositiveInfinity
}

/** Pair of values, where both values can be scattered across multiple nodes.
  */
case class ValuePair[+X, +Y](_1: Value[X], _2: Value[Y]) 
extends Value[(X, Y)] {
  def identifier = ??? // TODO
  def get(implicit ctx: TrivialContext): Future[(X, Y)] = {
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
case class Values[X, +CC[E] <: scavenger.GenericProduct[E, CC]]
(values: CC[Value[X]]) extends Value[CC[X]] {
  def identifier = ??? // TODO: need better CCC's now...
  def get(implicit ctx: TrivialContext): Future[CC[X]] = {
    import ctx.executionContext
    val fCbf = genProdCbf[CC, Future[X]](values)
    val futs = values.map(_.get(ctx))(fCbf)
    Future.sequence(futs)(genProdCbf[CC, X](values), ctx.executionContext)
  }
  protected[scavenger] def inputsInRam = 
    values.map(_.inputsInRam).foldLeft(Set.empty[Instance]){_ ++ _}
  protected[scavenger] def fullEvalSize = 
    values.map(_.fullEvalSize).foldLeft(GCS.zero[Instance])(_ + _)
}