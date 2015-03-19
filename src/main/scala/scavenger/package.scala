import scala.concurrent.Future
import scala.language.implicitConversions
import scavenger.categories.formalccc

/** Contains the API and an Akka-backend implementation of the 
  * Scavenger framework.
  */
package object scavenger {
  //type Identifier = formalccc.Elem

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

  // TODO: do we still need this? 
  // Would anyone remember it's there? (CRUFT?)
  def printingStackTrace[X](name: String)(f: => X): X = {
    println("BEGIN " + name)
    try {
      val x = f
      println("END " + name)
      x
    } catch {
      case t: Throwable => {
        println("Caught some exception: " + t) 
        t.printStackTrace
        throw new Exception("Failure when executing " + name)
      }
    }
  }
}