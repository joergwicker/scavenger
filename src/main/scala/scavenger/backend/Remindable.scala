package scavenger.backend

import akka.actor.Actor
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

case class Reminder (
  durationMillis: Long,
  message: String
)

/**
 * Trait for defensive slow polling.
 * 
 * Slow polling is used as backup strategy for the case that
 * the messages like "hey, there are jobs available!" get lost
 * somehow.
 *
 * In general, it prevents a node from trying something once, failing,
 * and then waiting forever for a response to a message that got lost.
 */
trait Remindable extends Actor {

  private var lastReminder: Long = 0L
  
  def isRelevant(reminder: Reminder): Boolean = {
    
    val now = System.currentTimeMillis
    val result = (now - lastReminder) > reminder.durationMillis
    lastReminder = now
    result
  }
  
  /**
   * Reminds itself to do something after the specified delay.
   *
   * The `reason` is currently not used, but might be logged later,
   * it serves documentation purposes.
   */
  def remindMyself(sec: Int, reason: String)(implicit exec: ExecutionContext): 
    Unit = {
    
    context.system.scheduler.scheduleOnce(sec seconds) {
      self ! new Reminder(sec * 1000L, reason)
    }
  }
}

