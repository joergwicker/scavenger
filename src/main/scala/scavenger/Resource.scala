package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scavenger.categories.formalccc

/**
 * `Resource` is a notion of computation that
 * incorporates the following three aspects.
 * First: a value of type `X` can be obtained
 * from a `Resource[X]` asynchronously, after
 * some potentially long computation.
 * Second: this computation can be distributed
 * across multiple compute nodes with different
 * capabilities.
 * Third: resources can be backed up and 
 * cached.
 *
 * Simplified, a `Resource[X]` can be thought
 * of as the following type:
 * {{{
 *   (Identifier[X], Context => Future[X])
 * }}}
 * where `Identifier[X]` is something that can
 * be used to identify saved or cached resources,
 * `Context` represents a compute node, and
 * `Future` is the future-monad. 
 */
trait Resource[+X] { outer =>

  /**
   * Formal expression that uniquely identifies this resource
   */
  def identifier: formalccc.Elem

  override def toString = "R(%s)".format(identifier.toString)

  /**
   * Start a concrete computation using context `ctx`,
   * that results in a value of type `X`
   */
  def compute(ctx: Context): Future[X]

  /**
   * Replace components of this `Resource` that are 
   * too complex. Used internally by `Scheduler`.
   */
  def simplify(
    cxt: Context, 
    mustBeReplaced: (CachingPolicy, Difficulty) => Boolean
  ): Future[Resource[X]]

  /**
   * Applies simplification to `this`, if necessary.
   */
  protected def simplifySelfIfNecessary(
    ctx: Context, 
    mustBeReplaced: (CachingPolicy, Difficulty) => Boolean
  ): Future[Resource[X]] = {
    if (mustBeReplaced(cachingPolicy, difficulty)) {
      ctx.asExplicitResource(this)
    } else {
      simplify(ctx, mustBeReplaced)
    }
  }

  /**
   * Get the caching policy of this resource
   */
  def cachingPolicy: CachingPolicy

  /**
   * Specifies whether this resource is difficult to 
   * compute. It determines whether the evaluation should 
   * be launched on a node that is responsible for coordination, 
   * or sent to a node responsible for the heavy number-crunching.
   */
  def difficulty: Difficulty

  /**
   * Returns a resource that looks exactly the same, except for
   * the changed caching policy.
   */
  def copy(newCachingPolicy: CachingPolicy): Resource[X] = 
    new Resource[X] {
      def identifier = outer.identifier
      def compute(ctx: Context) = outer.compute(ctx)
      def cachingPolicy = newCachingPolicy
      def difficulty = outer.difficulty
      def simplify(
        cxt: Context, 
        mustBeReplaced: (CachingPolicy, Difficulty) => Boolean
      ): Future[Resource[X]] = {
        val simplifiedOuter = outer.simplifySelfIfNecessary(ctx, mustBeReplaced)
        for(simpler <- simplifiedOuter) 
          yield simpler.copy(newCachingPolicy)
      }
    }

  /**
   * This is the most general method that allows to
   * transform `Resource`s.
   *
   * When a `Resource[X]` is mapped by an `Algorithm[X, Y]`,
   * the identifiers of the algorithm and the resources
   * are composed, and the result of type `X`
   * that is encapsulated
   * in the `Resource[X]` becomes the argument of the
   * algorithm's `apply` method, which then produces 
   * a value of type `Y` after some delay.
   *
   * Additional optimizations (caching, distribution) is
   * performed by the context, which is passed to the
   * `compute` method of the new resource.
   */
  private[scavenger] def flatMap[Y](
    algId: formalccc.Elem, 
    d: Difficulty
  )(
    f: (X, Context) => Future[Y]
  ): Resource[Y] = new Resource[Y] {
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
      cxt: Context, 
      mustBeReplaced: (CachingPolicy, Difficulty) => Boolean
    ): Future[Resource[X]] = {
      val simplifiedOuter = outer.simplifySelfIfNecessary(ctx, mustBeReplaced)
      for(simpler <- simplifiedOuter) 
        yield simpler.flatMap(algId, d)(f)
    }
    def cachingPolicy = CachingPolicy.Nowhere
    def difficulty = d
  }

  /**
   * Creates a resource that represents a pair of this resource and the
   * other resource
   */
  def zip[Y](other: Resource[Y]): Resource[(X, Y)] = ResourcePair(this, other)

  /**
   * Assuming that `X` is actually a function type `A => B`, 
   * allows to apply this resource to an `A`-valued one.
   *
   * The `CanApplyTo` is provided by a single implicit method 
   * in `package.scala`.
   */
  def apply[A, B](arg: Resource[A], difficulty: Difficulty = Cheap)(
    implicit cat: CanApplyTo[X, A, B]
  ): Resource[B] = cat(this, arg, difficulty)

}

object Resource {
  def apply[X](id: String, x: X): Resource[X] = 
    Value(new formalccc.Atom(id), x, CachingPolicy.Nowhere)
}

/**
 * This is the type-dependent polymorphism pattern used 
 * in the method `apply` of the `Resource` trait.
 * 
 * It ensures that only function-valued resources can be
 * applied to other resources.
 */
trait CanApplyTo[-Func, -In, +Out] {
  def apply(
    f: Resource[Func], 
    input: Resource[In], 
    d: Difficulty
  ): Resource[Out]
}