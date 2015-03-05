import scala.concurrent.Future
import scala.language.implicitConversions
import scavenger.categories.freeccc

package object scavenger {
  type Identifier[X] = freeccc.Arrow[Unit, X]

  // Three castings into the canoical form of a morphism
  implicit def withoutContextToFull[X, Y](f: X => Future[Y]): 
    ((X, Context) => Future[Y]) = {
    case (x, ctx) => f(x)
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
      def identifier = freeccc.Edge[X, Y](algorithmId)
      def difficulty = d
      def apply(x: X, ctx: Context) = f(x, ctx)
    }

  // Three different constructors for atomic algorithms
  def trivial[X, Y](algorithmId: String)(f: (X, Context) => Future[Y]):
    Algorithm[X, Y] = atomicAlgorithmConstructor(Trivial)(algorithmId, f)

  def heavy[X, Y](algorithmId: String)(f: (X, Context) => Future[Y]):
    Algorithm[X, Y] = atomicAlgorithmConstructor(ComputeHeavy)(algorithmId, f)

  def parallel[X, Y](algorithmId: String)(f: (X, Context) => Future[Y]):
    Algorithm[X, Y] = atomicAlgorithmConstructor(Parallelizable)(algorithmId, f)

  // providing implicit `CanApplyTo`s
  // for the `apply` method of `Resource` that allows to 
  // build `Y`-valued resources from `X`-valued and `Y => X`-valued ones.
  implicit def canApplyFunctionToArg[X, Y]: CanApplyTo[X => Y, X, Y] = 
  new CanApplyTo[X => Y, X, Y] {
    def apply(f: Resource[X => Y], x: Resource[X], d: Difficulty): 
    Resource[Y] = 
    Eval[X, Y](d)(ResourcePair(f, x))
  }
}