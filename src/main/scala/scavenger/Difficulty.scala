package scavenger

/** Describes the difficulty of a computation.
  *
  * This enumeration describes three types of difficulty
  * of the jobs. If a job is trivial, it can be executed
  * on either master and worker nodes.
  * If job is marked as parallelizable, it should be taken
  * care by the master.
  * If job is marked as expensive, it should be
  * evaluated on a worker node.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
sealed trait Difficulty
case object Expensive extends Difficulty
case object Parallel extends Difficulty
case object Cheap extends Difficulty
