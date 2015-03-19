package scavenger.backend.master

import scavenger._
import scavenger.backend._

/** Master-specific scheduler implementation
  * 
  * @since 2.1
  * @author Andrey Tyukin
  */
trait MasterScheduler extends Scheduler {

  protected def mustBeSimplified(
    policy: CachingPolicy,
    difficulty: Difficulty
  ): Boolean = {
    policy.cacheGlobally || policy.backup || (difficulty == Parallel)
  }

  protected def mustScheduleHere(
    policy: CachingPolicy, 
    difficulty: Difficulty
  ): Boolean = {
    (difficulty == Parallel) || (difficulty == Cheap)
  }
}
