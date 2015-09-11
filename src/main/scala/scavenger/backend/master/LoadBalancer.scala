package scavenger.backend.master

import akka.actor._
import scala.collection.mutable
import scala.collection.mutable.HashMap
import scala.concurrent.{Future, Promise, ExecutionContext}
import scavenger._
import scavenger.backend._
import scavenger.backend.worker.Worker.WorkerHere
import scavenger.categories.formalccc
import LastMessageTimeMonitoring.RemoteNodeNotResponding

/** This trait implements load balancing among multiple
  * worker nodes.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
trait LoadBalancer 
extends Actor 
with ActorLogging 
with SimpleComputationExecutor
with ContextProvider 
with Remindable
with LastMessageTimeMonitoring {

  import context.dispatcher
  
  /** Stores internal jobs that
    * have not yet been assigned to a worker
    */
  private val queue = mutable.Queue.empty[InternalJob]

  /** Assignment of worker-ActorRef's to the currently processed job.
    */
  private val assignedJobs: mutable.Map[ActorRef, Option[InternalJob]] = 
    HashMap.empty[ActorRef, Option[InternalJob]]

  /** Perform a simple computation that can be delegated.
    */
  def computeSimplified[X](r: Computation[X]): Future[X] = {
    // we simply create a promise in the promise-map, and enqueue the job
    val p = Promise[Any]
    val label = toInternalLabel(r.identifier)
    promises(label) = p
    enqueueSimple(label, r)
    val result = p.future.map{ a => a.asInstanceOf[X] }
    result
  }

  /** Appends an internal job id to a job and puts it into the job queue.
    */
  private def enqueueSimple(label: InternalLabel, job: Computation[Any]): Unit = 
  { 
    val internalJob = InternalJob(label, job)
    queue.enqueue(internalJob)
    // notify all workers that there is something to do
    for (worker <- idleWorkers) worker ! JobsAvailable
  }

  /** Assigns a job to worker.
    *
    * Just a way to make things a little safer (e.g. prevents you from
    * sending `Computation`s to workers).
    */
  private def sendJobToWorker(j: InternalJob, w: ActorRef): Unit = {
    log.debug("sendJobToWorker: job = {}",j)
    assert(j != null, "The job should be not null")
    w ! j
  }
  
  /** Makes sure that we know about the existence of the worker
    */
  protected[master] def register(worker: ActorRef): Unit = {
    context.watch(worker)
    if (!assignedJobs.contains(worker)) {
      assignedJobs(worker) = None
      log.info("Registered worker " + worker.path.name)
    }
  }

  /** Tries to assign a job to a worker.
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
    } else if (assignedJobs(worker).isEmpty){
      assert(!queue.isEmpty, "Queue should not be empty, size = " + queue.size)
      val internalJob = queue.dequeue
      assert(!(internalJob == null), "Dequeued job should be not null")
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

  /** Puts a job of a failed worker back into the queue
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
          enqueueSimple(oldJob.label, oldJob.job)
          assignedJobs(worker) = None
      }
    }
  }
  
  /** Returns collection with all idle workers
    */
  private def idleWorkers = 
    for ((w, None) <- assignedJobs) yield w
    
  /** Returns collection of all workers
    */
  private def allWorkers = assignedJobs.keySet
  
  /** Handles reminders sent after the initialization phase
    */
  protected def handleReminders: Receive = {
    // after we switch into normal operation mode, we should 
    // assign a job to all idle workers that joined the master
    // in the initialization phase
    case r: Reminder if (isRelevant(r)) =>
      val ws = idleWorkers
      log.info(
        s"Trying to assign ${queue.size} jobs from initialization phase " +
        s"to ${ws.size} workers: { " + ws.mkString(",") +" }" 
      )
      for (w <- ws) {
        tryAssignJob(w)
      }
    case r: Reminder => { /* no longer relevant, ignore */ }
  }
  
  /** Behavior for normal operation mode.
    *
    * Trying to assign jobs to workers,
    * withdrawing jobs from terminated workers.
    */
  protected[master] def handleWorkerRequests: Receive = {
    case WorkerHere =>
      log.info("Got job request from a worker " + sender.path.name)
      register(sender)
      tryAssignJob(sender)
      
    case Terminated(worker) if (assignedJobs.contains(worker)) => 
        withdrawJob(worker)

    case RemoteNodeNotResponding(worker) if(assignedJobs.contains(worker)) =>
      withdrawJob(worker)
  }
  
  /** Handles results from workers
    */
  protected[master] def handleWorkerResponses: Receive = {
    case InternalResult(label, result) => {
      val logMessageIntro = "Received result " + label + " from " + 
        sender.path.name + " "
      assignedJobs(sender) match {
        case None => log.error(
          logMessageIntro + " but there were no jobs assigned to this worker"
        )
        case Some(originalJob) => {
          if (originalJob.label != label) {
            log.error(
              logMessageIntro + " but the id was wrong: original = " + 
              originalJob.label + " returned = " + label
            )
            withdrawJob(sender)
          } else {
            log.info(
              logMessageIntro + 
              ", fulfilling promise, try assign new job. "
            )
            fulfillPromise(label, result)
            assignedJobs(sender) = None
            tryAssignJob(sender)
          }
        }
      }
    }
  } 
}
