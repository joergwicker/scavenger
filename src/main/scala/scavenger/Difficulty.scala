package scavenger

/** Describes the difficulty of a computation.
  *
  * This enumeration describes three types of difficulty
  * of the jobs. If a job is cheap, it can be executed
  * either on master or worker nodes.
  * If job is marked as parallelizable, it should be taken
  * care by the master.
  * If job is marked as expensive, it should be
  * evaluated on a worker node.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
sealed trait Difficulty

/** A cheap algorithm can be executed on both master 
  * and worker nodes.
  */
case object Cheap extends Difficulty

/** Expensive computations are always delegated to 
  * the worker nodes
  */
case object Expensive extends Difficulty

/** Parallel computations (i.e. algorithms that 
  * submit further jobs) are always executed on
  * the master node.
  */
case object Parallel extends Difficulty
