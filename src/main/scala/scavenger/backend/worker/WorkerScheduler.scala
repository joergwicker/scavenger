package scavenger.backend.worker

import scavenger.{CachingPolicy, Difficulty}
import scavenger.backend.Scheduler

trait WorkerScheduler extends Scheduler {

  /**
   * Worker nodes cannot delegate anything. 
   * Everything must be scheduled right here.
   */
  protected def mustScheduleHere(
    policy: CachingPolicy, 
    difficulty: Difficulty
  ): Boolean = true

  /** 
   * On a worker node, nothing has to be simplified
   */
  protected def mustBeSimplified(
    policy: CachingPolicy,
    difficulty: Difficulty
  ): Boolean = false
}