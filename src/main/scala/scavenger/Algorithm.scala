package scavenger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scavenger.categories.freeccc.Arrow

/**
 * Formal composition of atomic algorithms.
 */
trait Algorithm[-X, +Y] { outer =>
  def identifier: Arrow[X, Y]

  /**
   * Creates a new resource modified by this algorithm
   */
  def apply(resource: Resource[X]): Resource[Y] 

  /**
   * Composes with another algorithm (Order: this=first, other=second)
   */
  def andThen[Z](other: Algorithm[Y, Z]): Algorithm[X, Z] = 
  new Algorithm[X, Z] {
    def identifier = other.identifier o outer.identifier
    def apply(resource: Resource[X]) = other(outer(resource))
  }

  /**
   * Composes with another algorithm (Order: this=second, other=first)
   */
  def o[W](other: Algorithm[W, X]): Algorithm[W, Y] = other andThen this

  /**
   * Creates pair of two arrows with same domain
   */
  def zip[D <: X, Z](other: Algorithm[D, Z]): Algorithm[D, (Y, Z)] = 
    AlgorithmPair[D, Y, Z](this, other)

  /**
   * Glues two parallel arrows into one
   */
  def cross[A, B](other: Algorithm[A, B]): Algorithm[(X, A), (Y, B)] = 
    (this o Fst[X, A]) zip (other o Snd[X, A])

  def curryFst[A, B](a: Resource[A])(ccf: CanCurryFst[X, A, B, Y]):
    Algorithm[B, Y] = ccf(this, a)

  def currySnd[A, B](b: Resource[B])(ccs: CanCurrySnd[X, A, B, Y]):
    Algorithm[A, Y] = ccs(this, b)
}

/**
 * `CanCurryFst` is an implicitly generated argument that
 * ensures that we can curry only methods that actually take
 * product types as inputs.
 *
 * Implementations are provided in the `package.scala` file.
 */
trait CanCurryFst[-In, -InA, -InB, +Out] {
  def apply(
    f: Algorithm[In, Out],
    input: Resource[InA]
  ): Algorithm[InB, Out]
}

trait CanCurrySnd[-In, -InA, -InB, +Out] {
  def apply(
    f: Algorithm[In, Out],
    input: Resource[InB]
  ): Algorithm[InA, Out]
}

/**
 * This is the natural notion of a morphism between 
 * objects of type `Resource[X]` for some `X`.
 *
 * An `AtomicAlgorithm[X,Y]` describes how to obtain a value
 * of type `Y` from a value of type `X` using a 
 * context. The computation can in general take some time,
 * therefore the return type of the `apply` method is
 * `Future[Y]`. 
 *
 * Furthermore, an algorithm provides a symbolic 
 * identifier, that enables us to identify modified
 * resources, and load them from cache or a file, if
 * possible.
 */
abstract class AtomicAlgorithm[-X, +Y] 
extends Algorithm[X, Y] 
with ((X, Context) => Future[Y]) { outer => 
  def difficulty: Difficulty
  def apply(x: X, ctx: Context): Future[Y]

  def apply(resource: Resource[X]): Resource[Y] = {
    resource.flatMap(identifier, difficulty)(this)
  }
}

