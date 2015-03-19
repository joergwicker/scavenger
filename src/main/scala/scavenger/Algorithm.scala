package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scavenger.categories.formalccc

/** Arbitrarily complex composition (including pairs, projections, currying) of atomic algorithms.
  *
  * The most general method to transform a `Computation[X]` into `Computation[Y]`.
  *
  * @since 2.1
  * @author AndreyTyukin
  */
trait Algorithm[-X, +Y] { outer =>
  def identifier: formalccc.Elem

  /** Creates a new computation modified by this algorithm
    */
  def apply(computation: Computation[X]): Computation[Y] 

  /** Determines how every output of this algorithm is cached.
    */
  def cachingPolicy: CachingPolicy = CachingPolicy.Nowhere

  /** Composes with another algorithm (Order: this=first, other=second)
    */
  def andThen[Z](other: Algorithm[Y, Z]): Algorithm[X, Z] = 
  new Algorithm[X, Z] {
    def identifier = other.identifier o outer.identifier
    def apply(computation: Computation[X]) = other(outer(computation))
  }

  /** Composes with another algorithm (Order: this=second, other=first)
    */
  def o[W](other: Algorithm[W, X]): Algorithm[W, Y] = other andThen this

  /** Creates pair of two arrows with same domain
    */
  def zip[D <: X, Z](other: Algorithm[D, Z]): Algorithm[D, (Y, Z)] = 
    AlgorithmPair[D, Y, Z](this, other)

  /** Glues two parallel arrows into one
    */
  def cross[A, B](other: Algorithm[A, B]): Algorithm[(X, A), (Y, B)] = 
    (this o Fst[X, A]) zip (other o Snd[X, A])

  /* Doesn't work: variance doesn't work out.
  def curryFst[A, B](a: Computation[A])(ccf: CanCurryFst[X, A, B, Y]):
    Algorithm[B, Y] = ccf(this, a)

  def currySnd[A, B](b: Computation[B])(ccs: CanCurrySnd[X, A, B, Y]):
    Algorithm[A, Y] = ccs(this, b)
  */

  def curryFst[A, B](a: Computation[A])(implicit cbp: CanBuildProduct[A, B, X]):
  Algorithm[B, Y] =
  new Algorithm[B, Y] {
    def identifier = formalccc.Curry(outer.identifier)(a.identifier)
    def apply(b: Computation[B]) = outer(cbp(a, b))
  }

  def currySnd[A, B](b: Computation[B])(implicit cbp: CanBuildProduct[A, B, X]):
  Algorithm[A, Y] =
  new Algorithm[A, Y] {
    import formalccc.{Curry => FCurry, _}
    // this one is a little tricky, see p. 61 third black CS book
    def identifier = FCurry(outer.identifier o Pair(Snd, Fst))(b.identifier)
    def apply(a: Computation[A]) = outer(cbp(a, b))
  }

  /** Returns a an that looks exactly the same, except that the
    * output of this algorithm has a different caching policy.
    */
  private[scavenger] def withCachingPolicy(newCachingPolicy: CachingPolicy): 
  Algorithm[X, Y] = 
    new Algorithm[X, Y] {
      def identifier = outer.identifier
      override def cachingPolicy = newCachingPolicy
      def apply(computation: Computation[X]): Computation[Y] = 
        outer.apply(computation).withCachingPolicy(newCachingPolicy)
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
}

/** `CanCurryFst` is an implicitly generated argument that
  * ensures that we can curry only methods that actually take
  * product types as inputs.
  *
  * Implementations are provided in the `package.scala` file.
  */
/*
trait CanCurryFst[+In, -InA, -InB, +Out] {
  def apply(
    f: Algorithm[In, Out],
    input: Computation[InA]
  ): Algorithm[InB, Out]
}

trait CanCurrySnd[+In, -InA, -InB, +Out] {
  def apply(
    f: Algorithm[In, Out],
    input: Computation[InB]
  ): Algorithm[InA, Out]
}
*/

trait CanBuildProduct[-A, -B, +Prod] {
  def apply(a: Computation[A], b: Computation[B]): Computation[Prod]
}

/** This is the natural notion of a morphism between
  * objects of type `Computation[X]` for some `X`.
  *
  * An `AtomicAlgorithm[X,Y]` describes how to obtain a value
  * of type `Y` from a value of type `X` using a
  * context. The computation can in general take some time,
  * therefore the return type of the `apply` method is
  * `Future[Y]`.
  *
  * Furthermore, an algorithm provides a symbolic
  * identifier, that enables us to identify modified
  * computations, and load them from cache or a file, if
  * possible.
  */
abstract class AtomicAlgorithm[-X, +Y] 
extends Algorithm[X, Y] 
with ((X, Context) => Future[Y]) { outer => 
  def difficulty: Difficulty
  def apply(x: X, ctx: Context): Future[Y]

  def apply(computation: Computation[X]): Computation[Y] = {
    computation.flatMap(identifier, difficulty)(this)
  }
}

