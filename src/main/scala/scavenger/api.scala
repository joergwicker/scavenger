import scala.language.higherKinds
import scala.concurrent.{Future, ExecutionContext}
import scala.language.implicitConversions

trait API {

  type DistributedJob[+X] <: DistributedJobOps[X]
  type LocalJob[+X] <: LocalJobOps[X] with DistributedJob[X]
  type TrivialJob[+X] <: TrivialJobOps[X] with LocalJob[X]

  protected trait DistributedJobOps[+X] {
    def zip[Y](other: DistributedJob[Y]): DistributedJob[(X, Y)]
  }

  protected trait LocalJobOps[+X] {
    def zip[Y](other: LocalJob[Y]): LocalJob[(X, Y)]
    def zip[Y](other: DistributedJob[Y]): DistributedJob[(X, Y)]
  }

  protected trait TrivialJobOps[+X] {
    def zip[Y](other: TrivialJob[Y]): TrivialJob[(X, Y)]
    def zip[Y](other: LocalJob[Y]): LocalJob[(X, Y)]
    def zip[Y](other: DistributedJob[Y]): DistributedJob[(X, Y)]
  }

  trait TrivialContext {
    def executionContext: ExecutionContext
  }

  trait LocalContext extends TrivialContext {
    def submit[X](job: LocalJob[X]): Future[X]
    def computeValue[X](job: LocalJob[X]): Future[Value[X]]
  }

  trait DistributedContext extends LocalContext {
    def submit[X](job: DistributedJob[X]): Future[X]
    def computeValue[X](job: DistributedJob[X]): Future[Value[X]]
  }

  trait Value[+X] {
    def get(ctx: TrivialContext): Future[X]
  }

  type DistributedAlgorithm[-X, +Y] <: DistributedAlgorithmOps[X, Y]
  type LocalAlgorithm[-X, +Y] <: LocalAlgorithmOps[X, Y] with DistributedAlgorithm[X, Y]
  type TrivialAlgorithm[-X, +Y] <: TrivialAlgorithmOps[X, Y] with LocalAlgorithm[X, Y]

  protected trait DistributedAlgorithmOps[-X, +Y] {
    def compose[W](prepended: DistributedAlgorithm[W, X]): DistributedAlgorithm[W, Y]
    def apply(x: DistributedJob[X]): DistributedJob[Y]
    def zip[V <: X, Z](other: DistributedAlgorithm[V, Z]): DistributedAlgorithm[V, (Y, Z)]
  }

  protected trait LocalAlgorithmOps[-X, +Y] {
    def compose[W](prepended: LocalAlgorithm[W, X]): LocalAlgorithm[W, Y]
    def compose[W](prepended: DistributedAlgorithm[W, X]): DistributedAlgorithm[W, Y]
    def apply(x: LocalJob[X]): LocalJob[Y]
    def apply(x: DistributedJob[X]): DistributedJob[Y]
    def zip[V <: X, Z](other: LocalAlgorithm[V, Z]): LocalAlgorithm[V, (Y, Z)]
    def zip[V <: X, Z](other: DistributedAlgorithm[V, Z]): DistributedAlgorithm[V, (Y, Z)]
  }

  protected trait TrivialAlgorithmOps[-X, +Y] {
    def compose[W](prepended: TrivialAlgorithm[W, X]): TrivialAlgorithm[W, Y]
    def compose[W](prepended: LocalAlgorithm[W, X]): LocalAlgorithm[W, Y]
    def compose[W](prepended: DistributedAlgorithm[W, X]): DistributedAlgorithm[W, Y]
    def apply(x: TrivialJob[X]): TrivialJob[Y]
    def apply(x: LocalJob[X]): LocalJob[Y]
    def apply(x: DistributedJob[X]): DistributedJob[Y]
    def zip[V <: X, Z](other: TrivialAlgorithm[V, Z]): TrivialAlgorithm[V, (Y, Z)]
    def zip[V <: X, Z](other: LocalAlgorithm[V, Z]): LocalAlgorithm[V, (Y, Z)]
    def zip[V <: X, Z](other: DistributedAlgorithm[V, Z]): DistributedAlgorithm[V, (Y, Z)]
  }

  // def sequence(jobs: Seq[Job[X]]): Job[Seq[X]] // for our generic collection type
  // def trivial[X, Y](id: String)(f: (X, TrivialContext) => Future[Y]): TrivialAlgorithm[X, Y]
  // def local[X, Y](id: String)(f: (X, LocalContext) => Future[Y]): LocalAlgorithm[X, Y]
  // def distributed[X, Y](id: String)(f: (X, DistributedContext) => Future[Y]): DistributedAlgorithm[X, Y]

  trait AtomicTrivialAlgorithm[-X, +Y] {
    def compute(x: X)(implicit ctx: TrivialContext): Future[Y]
  }

  implicit def embedAtomicTrivialAlgorithm[X, Y](atomic: AtomicTrivialAlgorithm[X, Y]): TrivialAlgorithm[X, Y]
  
}

println("it compiles, ship it.")