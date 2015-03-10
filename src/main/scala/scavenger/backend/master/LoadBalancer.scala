package scavenger.backend.master

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Terminated
import scala.collection.mutable
import scala.collection.mutable.HashMap
import Master.{Job, LabeledJob, LabeledResult}
import ExternalInterface.{NoExternalLabel, PromisedSubjob}
import scavenger.util.Remindable
import scavenger.util.Remindable.Reminder
import scavenger.util.LastMessageTimeMonitoring
import scavenger.worker.Worker.WorkerHere
import scavenger.worker.Worker.InternalResult

/**
 * This trait implements load balancing of internal
 * jobs between the workers.
 */
private[scavenger] trait LoadBalancing 
extends Actor 
with ActorLogging 
with Remindable 
with LastMessageTimeMonitoring 
with MasterCache {
    
  import LoadBalancing._

  /**
   * Internal job id's
   */
  private var jobId: Long = 0L
  private def nextJobId() = {jobId += 1; jobId}
  
  /** 
   * Stores internal jobs that 
   * have not yet been assigned to a worker 
   */ 
  private val queue = mutable.Queue.empty[InternalJob]
  
  /**
   * Assignment of worker-ActorRef's to the currently processed job.
   * 
   * Registered workers that currently have no job are stored as
   * `
   */
  private val assignedJobs: mutable.Map[ActorRef, Option[InternalJob]] = 
    HashMap.empty[ActorRef, Option[InternalJob]]
  
  /**
   * Appends an internal job id to a job and puts it into the job queue.
   */
  protected[master] def enqueue(job: PromisedSubjob): Unit = { 
    val internalJob = InternalJob(job.job, nextJobId())
    queue.enqueue(internalJob)
    log.info("enqueued job " + job.job)
    // notify all workers that there is something to do
    for ((worker, None) <- assignedJobs) worker ! JobsAvailable
  }
  
  /**
   * Assigns a job to worker.
   * 
   * Just a way to make things a little safer (e.g. prevents you from
   * sending [[ExternalInterface.PromisedSubjob]]s to workers).
   */
  private def sendJobToWorker(j: InternalJob, w: ActorRef): Unit = w ! j
  
  /**
   * Makes sure that we know about the existence of the worker
   */
  protected[master] def register(worker: ActorRef): Unit = {
    context.watch(worker)
    if (!assignedJobs.contains(worker)) {
      assignedJobs(worker) = None
      log.info("Registered worker " + worker.path.name)
    }
  }
  
  /**
   * Tries to assign a job to a worker.
   * Sends an `NothingToDo` reply if there is currently nothing to do.
   */
  private def tryAssignJob(worker: ActorRef): Unit = {
    if (queue.isEmpty) {
      log.info("Currently nothing to do for " + worker.path.name)
      worker ! NoJobsAvailable
    } else if (!assignedJobs.contains(worker)) {
      log.error(
        "Attempted to assign job to unregistered worker " + 
        worker.path.name
      )
    } else if (assignedJobs(worker) == None){
      val internalJob = queue.dequeue
      assignedJobs(worker) = Some(internalJob)
      sendJobToWorker(internalJob, worker)
      log.info(
        "Assigned job " + internalJob.job + 
        " to " + worker.path.name
      )
    } else {
      log.info(
        "Worker is already occupied, " +
        "no new job assignment for " + 
        worker.path.name
      )
    }
  }

  /**
   * Puts a job of a failed worker back into the queue
   */
  private def withdrawJob(worker: ActorRef): Unit = {
    if (!assignedJobs.contains(worker)) {
      log.error(
        "Attempted to withdraw a job " +
        "from a non-registered Worker {}", 
        worker.path.name
      )
    } else {
      assignedJobs(worker) match {
        case None => // do nothing
          log.info("Tried to withdraw job from " + worker + 
            " (nothing to withdraw)")
        case Some(oldJob) =>
          log.info("Withdrawing and re-enqueueing job from " + worker)
          enqueue(PromisedSubjob(
            oldJob.job, 
            Nil,
            true,
            "recovering from failure of " + worker :: Nil
          ))
          assignedJobs(worker) = None
      }
    }
  }
  
  /**
   * Returns collection with all idle workers
   */
  protected[master] def freeWorkers = 
    for ((w, None) <- assignedJobs) yield w.path.name
    
  /**
   * Returns collection of all workers
   */
  protected[master] def allWorkers = assignedJobs.keySet
  
  /**
   * Handles reminders sent after the initialization phase
   */
  protected[master] def handleReminders: Receive = {
    // after we switch into normal operation mode, we should 
    // assign a job to all idle workers that joined the master
    // in the initialization phase
    case r: Reminder if (isRelevant(r)) =>
      val ws = freeWorkers
      log.info(
        s"Trying to assign ${queue.size} jobs from initialization phase " +
        s"to ${ws.size} workers: { " + ws.mkString(",") +" }" 
      )
      for ((w, None) <- assignedJobs) {
        tryAssignJob(w)
      }
  }
  
  /**
   * Behavior for normal operation mode.
   * 
   * Trying to assign jobs to workers, 
   * withdrawing jobs from terminated workers.
   */
  protected[master] def handleWorkerRequests: Receive = 
    updatingLastMessageTime {
    
    case WorkerHere =>
      log.info("Got job request from a worker " + sender.path.name)
      register(sender)
      tryAssignJob(sender)
      
    case Terminated(worker) if (assignedJobs.contains(worker)) => 
      withdrawJob(worker)
  }
  
  /**
   * Handles results from workers
   */
  protected[master] def handleWorkerResponses: 
    Receive = updatingLastMessageTime {
    case res @ InternalResult(result, id) => {
      val intro = "Received result " + result + " from " + 
        sender.path.name + " "
      assignedJobs(sender) match {
        case None => log.error(
          intro + " but there were no jobs assigned to this worker"
        )
        case Some(originalJob) => {
          if (originalJob.internalId != id) {
            log.error(
              intro + " but the id was wrong: original = " + 
              originalJob.internalId + 
              " returned = " + id
            )
            withdrawJob(sender)
          } else {
            log.info(intro + ", fulfilling promise, try assign new job")
            fulfillPromisedSubjob(result.id, result.value)
            assignedJobs(sender) = None
            tryAssignJob(sender)
          }
        }
      }
    }
  }
  
}

private[scavenger] object LoadBalancing {
  
  /**
   * Messages used for communication with the workers.
   */
  private[scavenger] case class InternalJob(job: Job, internalId: Long)
  
  /**
   * Message that tells the worker that there is currently nothing to do.
   */
  private[scavenger] case object NoJobsAvailable
  
  /**
   * Message that is broadcast to all workers when there are new 
   * jobs available
   */
  private[scavenger] case object JobsAvailable
}
