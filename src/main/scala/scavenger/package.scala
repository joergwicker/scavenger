import scala.concurrent.Future
import scala.language.implicitConversions
import scala.language.higherKinds
import scavenger.categories.formalccc

/** Contains the API and an Akka-backend implementation of the 
  * Scavenger framework.
  */
package object scavenger /* TODO: why was it here??: extends Serializable */ {
  type Identifier = String

  // Three castings into the canonical form of a morphism
  implicit def withoutContextToFull[X, Y](f: X => Future[Y]): 
    ((X, Context) => Future[Y]) = {
    // case (x, ctx) => f(x)
    throw new UnsupportedOperationException(
      "Attempted to use a function of type X => Future[Y] as Scavenger-Algorithm. " + 
      "Notice that the definition of the function closes over some execution context, " + 
      "which can not be easily serialized and sent over the wire. Please change the " +
      "type to (X, Context) => Future[Y], and use the executionContext provided by " +
      "the Scavenger `Context`."
    )
  }

  implicit def synchronousToFull[X, Y](f: (X, Context) => Y): 
    ((X, Context) => Future[Y]) = {
    case (x, ctx) => Future(f(x, ctx))(ctx.executionContext)
  }

  implicit def simpleToFull[X, Y](f: X => Y):
    ((X, Context) => Future[Y]) = {
    case (x, ctx) => Future(f(x))(ctx.executionContext)
  }

  // Generic atomic algorithm constructor that builds 
  // Atomic algorithms from functions.
  private def atomicAlgorithmConstructor[X, Y](d: Difficulty)(
    algorithmId: String, f: (X, Context) => Future[Y]
  ): Algorithm[X, Y] = new AtomicAlgorithm[X, Y] {
      def identifier = formalccc.Atom(algorithmId)
      def difficulty = d
      def apply(x: X, ctx: Context) = f(x, ctx)
    }

  // Three different constructors for atomic algorithms
  /** Constructs a cheap atomic algorithm with specified identifier */
  def cheap[X, Y](algorithmId: String)(f: (X, Context) => Future[Y]):
    Algorithm[X, Y] = atomicAlgorithmConstructor(Cheap)(algorithmId, f)

  /** Constructs an expensive atomic algorithm with specified identifier */
  def expensive[X, Y](algorithmId: String)(f: (X, Context) => Future[Y]):
    Algorithm[X, Y] = atomicAlgorithmConstructor(Expensive)(algorithmId, f)

  /** Constructs a parallelizable atomic algorithm with specified identifier */
  def parallel[X, Y](algorithmId: String)(f: (X, Context) => Future[Y]):
    Algorithm[X, Y] = atomicAlgorithmConstructor(Parallel)(algorithmId, f)

  /** Provides implicit `CanApplyTo`s
    * for the `apply` method of `Computation` that allows to 
    * build `Y`-valued computations from `X`-valued and `Y => X`-valued ones.
    */
  implicit def canApplyFunctionToArg[X, Y]: CanApplyTo[X => Y, X, Y] = 
  new CanApplyTo[X => Y, X, Y] {
    def apply(f: Computation[X => Y], x: Computation[X], d: Difficulty): 
    Computation[Y] = 
    Eval[X, Y](d)(ComputationPair(f, x))
  }

  implicit def canBuildCouple[A, B]: CanBuildProduct[A, B, (A, B)] = 
  new CanBuildProduct[A, B, (A, B)] {
    def apply(a: Computation[A], b: Computation[B]): Computation[(A, B)] =
      ComputationPair(a, b)
  }

  import scala.collection.GenTraversable
  import scala.collection.IndexedSeqLike
  import scala.collection.generic.CanBuildFrom
  import scala.collection.mutable.Builder
  import scala.collection.generic.GenericTraversableTemplate

  /**
   * This is an interface for a collection type that can be conveniently 
   * used for products (in the sense of cartesian closed categories)
   * with more than two components. It should at least subsume `Vector` and
   * wrapped arrays.
   *
   * The `IndexedSeqLike` makes sure that we can quickly access elements of
   * the product by index (necessary to implement projections).
   * 
   * Moreover, `IndexedSeqLike` makes sure that the right type of collection
   * is used when we try to apply `map` and `flatMap`. If we used 
   * `IndexedSeq` instead, we would lose the exact type of the collection
   * (e.g. `WrappedArray` or `Vector`).
   *
   * `GenericTraversableTemplate` is there because it provides a 
   * polymorphic `genericBuilder`.
   *
   * 
   */
  private[scavenger] type GenericProduct[E, +CC[X] <: IndexedSeq[X]] = 
    IndexedSeq[E] with
    IndexedSeqLike[E, CC[E]] with 
    GenericTraversableTemplate[E, CC]

  /** Special CBF's used inside collection-valued jobs.
    *
    */
  private[scavenger] def genProdCbf[CC[X] <: GenericProduct[X, CC], Y] =
    new CanBuildFrom[CC[_], Y, CC[Y]] {
      def apply(): Builder[Y, CC[Y]] = {
        throw new AssertionError(
          "'This should never happen: genProdCbf.apply()' " + 
          "Something or someone attempted to call `CanBuildFrom.apply()` on " +
          "a `scavenger.genProdCbf`. However, `CanBuildFrom[_,_,_]` is " +
          "contravariant in the first parameter, but `Unit` is never a " +
          "subclass for any collection type. Therefore, forcing each " +
          "`CanBuildFrom[X,...]` to be a `CanBuildFrom[Unit,...] " +
          "is just pure evil, so this operation is not supported, " +
          "and should never be used. " +
          "If you see this message, then there is a bug in the framework. "
        )
      }
      def apply(coll: CC[_]): Builder[Y, CC[Y]] = coll.genericBuilder[Y]
    }
}
