package hotswapWorker
trait Job[+X] {
  def doJob(): X
}
