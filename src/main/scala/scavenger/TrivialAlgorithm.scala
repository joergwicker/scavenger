package scavenger 

sealed trait TrivialAlgorithm[-X, +Y] {
  
}

abstract class TrivialAtomicAlgorithm[-X, +Y] extends TrivialAlgorithm[X, Y]