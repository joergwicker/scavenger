package scavenger

sealed trait Difficulty
object Cheap extends Difficulty
object Expensive extends Difficulty
object Parallel extends Difficulty