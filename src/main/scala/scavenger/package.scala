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
  def cheap[X, Y](algorithmId: String)(f: (X, Context) => Future[Y]):
    Algorithm[X, Y] = atomicAlgorithmConstructor(Cheap)(algorithmId, f)

  def expensive[X, Y](algorithmId: String)(f: (X, Context) => Future[Y]):
    Algorithm[X, Y] = atomicAlgorithmConstructor(Expensive)(algorithmId, f)

  def parallel[X, Y](algorithmId: String)(f: (X, Context) => Future[Y]):
    Algorithm[X, Y] = atomicAlgorithmConstructor(Parallel)(algorithmId, f)

  // providing implicit `CanApplyTo`s
  // for the `apply` method of `Resource` that allows to 
  // build `Y`-valued resources from `X`-valued and `Y => X`-valued ones.
  implicit def canApplyFunctionToArg[X, Y]: CanApplyTo[X => Y, X, Y] = 
  new CanApplyTo[X => Y, X, Y] {
    def apply(f: Resource[X => Y], x: Resource[X], d: Difficulty): 
    Resource[Y] = 
    Eval[X, Y](d)(ResourcePair(f, x))
  }

  implicit def canCurryXYtoZintoXtoYtoZ[X, Y, Z]: CanCurryFst[(X,Y), X, Y, Z] =
  new CanCurryFst[(X, Y), X, Y, Z] {
    def apply(
      f: Algorithm[(X, Y), Z],
      x: Resource[X]
    ) = new Algorithm[Y, Z] {
      def identifier = freeccc.Curry(f.identifier) o x.identifier
      def apply(y: Resource[Y]) = f(ResourcePair(x, y))
    }
  }

  implicit def canCurryXYtoZintoYtoXtoZ[X, Y, Z]: CanCurrySnd[(X,Y), X, Y, Z] = 
  new CanCurrySnd[(X,Y), X, Y, Z] {
    def apply(
      f: Algorithm[(X, Y), Z],
      y: Resource[Y]
    ) = new Algorithm[X, Z] {

      // this one is a little tricky, see p. 61 third black CS book
      def identifier = freeccc.Curry(
        f.identifier o 
        freeccc.Pair(freeccc.Snd[Y, X], freeccc.Fst[Y, X])
      ) o y.identifier

      def apply(x: Resource[X]) = f(ResourcePair(x, y))
    }
  }
}