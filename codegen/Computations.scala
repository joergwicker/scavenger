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
  def submit[X](c: SimpleComputation[X]): Future[FinishedComputation[X]]
  def eval[X](c: SimpleComputation[X]): Future[X] = {
    for {
      finished <- this.submit()
    }
  }
}

/** Computations performed on worker nodes, which can contain cached parts.
  */
trait LocalEvaluator extends SimpleEvaluator {
  def submit[X](c: LocalComputation[X]): Future[X]
}

/** Most general kind of computation */
trait DistributedEvaluator {
  def submit[X](c: DistributedComputation[X]): Future[X]
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

trait FinishedComputation[+X] extends TrivialComputation[X] {
  def getValue(exCtx: ExecutionContext): Future[X]
  final def compute(trivEval: TrivialEvaluator): Future[X] = getValue(trivEval)
}

trait DistributedAlgorithm[-X, +Y] {
  def apply(dc: DistributedComputation[X]): DistributedComputation[Y] =
    DistributedApply(this, dc)
}

trait LocalAlgorithm[-X, +Y] {
  def apply(lc: LocalComputation[X]): LocalComputation[Y] = LocalApply(lc)
  def apply(dc: DistributedComputation[X]): DistributedComputation[Y] = {
    dc match {
      case lc: LocalComputation[X] => LocalApply(this, lc)
      case dc: DistributedComputation[X] => DistributedApply(this, dc)
    }
  }
}

final case class DistributedApply[X, +Y](
  alg: DistributedAlgorithm[X, Y],
  comp: DistributedComputation[X]
) extends DistributedComputation[Y] {
  def compute(distEval: DistributedEvaluator): Future[Y] = {
    comp match {
      case finished: FinishedComputation[X] => 
        for {
          x <- finished.getValue
          y <- alg.compute(x, distEval)
        } yield y
      case _ => 
        for {
          x <- distEval.submit(comp)
          y <- distEval.submit(alg(x))
        } yield y
    }
  }
}

final case class LocalApply[X, +Y](
  alg: LocalAlgorithm[X, Y],
  comp: LocalComputation[X]
) extends LocalComputation[Y] {
  def compute(localEval: LocalEvaluator): Future[Y] = {
    comp match {
      case finished: TrivialComputation[X] =>
         for {
          x <- finished.getValue
          y <- alg.compute(x, localEval)
        } yield y
      case nontrivial => 
        for {
          x <- localEval.submit(nontrivial)
          y <- localEval.submit(alg(x))
        } yield y
    }
  }
}

final case class TrivialApply[X, +Y](
  alg: TrivialAlgorithm[X, Y],
  comp: TrivialComputation[X]
) extends TrivialComputation[Y] {
  def compute(trivEval: TrivialEvaluator): Future[Y] = {
    for {
      x <- comp.getValue
      y <- alg.compute(x, trivEval)
    } yield y
  }
}