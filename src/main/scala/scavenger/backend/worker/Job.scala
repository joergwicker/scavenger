package scavenger.backend.worker
trait Job[+X] {
  def doJob(): X
}
