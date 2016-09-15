import scala.concurrent.Future

trait ComputationIdentifier[+X]
trait AlgorithmIdentifier[+X]

/** Only execution context for futures, nothing else */
trait TrivialEvaluator {
  def eval[X](c: TrivialComputation[X]): Future[X]
}

/** Only execution context for futures, guarantee that the computation is
  * performed on a node separate from the master node. No caching allowed.
  */
trait SimpleEvaluator extends TrivialEvaluator {
  def eval[X](c: SimpleComputation[X]): Future[X]
}

/** Computations performed on worker nodes, which can contain cached parts.
  */
trait LocalEvaluator extends SimpleEvaluator {
  def eval[X](c: LocalComputation[X]): Future[X]
}

/** Most general kind of computation */
trait DistributedEvaluator {
  def eval[X](c: DistributedComputation[X]): Future[X]
}

trait DistributedComputation[+X] {
  def id: ComputationIdentifier[X]
  def compute(distEval: DistributedEvaluator): Future[X]
}

trait LocalComputation[+X] extends DistributedComputation[X] {
  def compute(localEval: LocalEvaluator): Future[X]
  final def compute(distEval: DistributedEvaluator): Future[X] = 
    compute(distEval.asInstanceOf[LocalEvaluator])
}

trait SimpleComputation[+X] extends LocalComputation[X] {
  def compute(simpleEval: SimpleEvaluator): Future[X]
  final def compute(localEval: LocalEvaluator) = 
    compute(localEval.asInstanceOf[SimpleEvaluator])
}

trait TrivialComputation[+X] extends SimpleComputation[X] {
  def compute(trivEval: TrivialEvaluator): Future[X]
  final def compute(simpEval: SimpleEvaluator) =
    compute(simpEval.asInstanceOf[TrivialEvaluator])
}

trait DistributedAlgorithm[-X, +Y] {
  def _applyDist(c: DistributedComputation[X]): DistributedComputation[Y]
  def apply(c: DistributedComputation[X]): DistributedComputation[Y] =
    _applyDist(c)
}

trait LocalAlgorithm[-X, +Y] {
  protected def _applyLoc(lc: LocalComputation[X]): LocalComputation[Y]
  protected def _applyDist(dc: DistributedComputation[X]): DistributedComputation[Y]
  def apply(lc: LocalComputation[X]): LocalComputation[Y] = _applyLoc(lc)
  def apply(dc: DistributedComputation[X]): DistributedComputation[Y] = {
    dc match {
      case lc: LocalComputation[X] => _applyLoc(lc)
      case dc: DistributedComputation[X] => _applyDist(dc)
    }
  }
}