package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scavenger.categories.formalccc

/** Interface for describing computation-valued requests to the 
  * Scavenger service.
  *
  *`Computation` is a notion of computation that
  * incorporates the following three aspects.
  * First: a value of type `X` can be obtained
  * from a `Computation[X]` asynchronously, after
  * some potentially long execution.
  * Second: this computation can be distributed
  * across multiple compute nodes with different
  * capabilities.
  * Third: computations can be backed up and
  * cached.
  *
  * Simplified, a `Computation[X]` can be thought
  * of as the following type:
  * {{{
  *   (Identifier[X], Context => Future[X])
  * }}}
  * where `Identifier[X]` is something that can
  * be used to identify saved or cached computations,
  * `Context` represents a compute node, and
  * `Future` is the future-monad.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
trait Computation[+X] extends scala.Serializable { outer =>

  /** Formal expression that uniquely identifies this computation
    */
  def identifier: formalccc.Elem

  override def toString = "Computation{%s}".format(identifier.toString)

  /** Start a concrete computation using context `ctx`,
    * that results in a value of type `X`
    */
  def compute(ctx: Context): Future[X]

  /** Replace components of this `Computation` that are
    * too complex. Used internally by `Scheduler`.
    */
  def simplify(
    cxt: Context, 
    mustBeReplaced: (CachingPolicy, Difficulty) => Boolean
  ): Future[Computation[X]]

  /** Applies simplification to `this`, if necessary.
    */
  private[scavenger] def simplifySelfIfNecessary(
    ctx: Context, 
    mustBeReplaced: (CachingPolicy, Difficulty) => Boolean
  ): Future[Computation[X]] = {
    if (mustBeReplaced(cachingPolicy, difficulty)) {
      ctx.asExplicitComputation(this)
    } else {
      simplify(ctx, mustBeReplaced)
    }
  }

  /** Get the caching policy of this computation
    */
  def cachingPolicy: CachingPolicy

  /** Specifies whether this computation is difficult to
    * compute. It determines whether the evaluation should
    * be launched on a node that is responsible for coordination,
    * or sent to a node responsible for the heavy number-crunching.
    */
  def difficulty: Difficulty

  /** Returns a computation that looks exactly the same, except for
    * the changed caching policy.
    */
  private[scavenger] def withCachingPolicy(newCachingPolicy: CachingPolicy): 
  Computation[X] = 
    new Computation[X] {
      def identifier = outer.identifier
      def compute(ctx: Context) = outer.compute(ctx)
      def cachingPolicy = newCachingPolicy
      def difficulty = outer.difficulty
      def simplify(
        ctx: Context, 
        mustBeReplaced: (CachingPolicy, Difficulty) => Boolean
      ): Future[Computation[X]] = {
        import ctx.executionContext
        val simplifiedOuter = outer.simplifySelfIfNecessary(ctx, mustBeReplaced)
        for(simpler <- simplifiedOuter) 
          yield simpler.withCachingPolicy(newCachingPolicy)
      }
    }

  /** Creates new computation that does exactly the same, but is additionally
    * cached on the Master node.
    */
  def cacheGlobally = withCachingPolicy(cachingPolicy.copy(cacheGlobally=true))
 
  /** Creates new computation that does exactly the same, but is additionally
    * cached on the worker nodes.
    */
  def cacheLocally = withCachingPolicy(cachingPolicy.copy(cacheLocally = true)) 

  /** Instructs the master node to back up the result of this computation
    */
  def backUp = withCachingPolicy(cachingPolicy.copy(backup = true))

  /** This is the most general method that allows to
    * transform `Computation`s.
    *
    * When a `Computation[X]` is mapped by an `Algorithm[X, Y]`,
    * the identifiers of the algorithm and the computations
    * are composed, and the result of type `X`
    * that is encapsulated
    * in the `Computation[X]` becomes the argument of the
    * algorithm's `apply` method, which then produces
    * a value of type `Y` after some delay.
    *
    * Additional optimizations (caching, distribution) is
    * performed by the context, which is passed to the
    * `compute` method of the new computation.
    */
  private[scavenger] def flatMap[Y](
    algId: formalccc.Elem, 
    d: Difficulty
  )(
    f: (X, Context) => Future[Y]
  ): Computation[Y] = new Computation[Y] {
    def identifier = algId(outer.identifier)
    def compute(ctx: Context): Future[Y] = {
      import ctx.executionContext
      for {
        // cxt.submit(x) is guaranteed to be equivalent to x.compute(ctx)
        x <- ctx.submit(outer)
        y <- f(x, ctx) 
      } yield y
    }
    def simplify(
      ctx: Context, 
      mustBeReplaced: (CachingPolicy, Difficulty) => Boolean
    ): Future[Computation[Y]] = {
      import ctx.executionContext
      val simplifiedOuter = outer.simplifySelfIfNecessary(ctx, mustBeReplaced)
      for(simpler <- simplifiedOuter) 
        yield simpler.flatMap(algId, d)(f)
    }
    def cachingPolicy = CachingPolicy.Nowhere
    def difficulty = d
  }

  /** Creates a computation that represents a pair of this computation and the
    * other computation
    */
  def zip[Y](other: Computation[Y]): Computation[(X, Y)] = 
    ComputationPair(this, other)

  /** Assuming that `X` is actually a function type `A => B`,
    * allows to apply this computation to an `A`-valued one.
    *
    * The `CanApplyTo` is provided by a single implicit method
    * in `package.scala`.
    */
  def apply[A, B](arg: Computation[A], difficulty: Difficulty = Cheap)(
    implicit cat: CanApplyTo[X, A, B]
  ): Computation[B] = cat(this, arg, difficulty)

}

object Computation {
  def apply[X](id: String, x: X): Computation[X] = 
    OldValue(new formalccc.Atom(id), x, CachingPolicy.Nowhere)

  /** Creates an ad-hoc computation with UUID as identifier
    */
  def apply[X](x: X): Computation[X] = {
    val uuid = java.util.UUID.randomUUID.toString
    apply(uuid, x)
  }
}

/** This is the type-dependent polymorphism pattern used
  * in the method `apply` of the `Computation` trait.
  *
  * It ensures that only function-valued computations can be
  * applied to other computations.
  */
trait CanApplyTo[-Func, -In, +Out] {
  def apply(
    f: Computation[Func], 
    input: Computation[In], 
    d: Difficulty
  ): Computation[Out]
}
