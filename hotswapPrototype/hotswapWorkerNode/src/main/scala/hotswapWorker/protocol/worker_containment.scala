package hotswapWorker.protocol

/** Messages used for communication between `Worker` and `Containment`
  *
  */
package worker_containment {
  sealed trait WorkerToContainment extends Direction
  sealed trait ContainmentToWorker extends Direction
  
  /** Tells the containment to load and execute a job
    *
    */
  case class LoadJob(serializedRepresentation: Array[Byte])
  extends WorkerToContainment
  
  /** A message sent by a Containment to WorkerNodeManager after the 
    * worker receives a serialized job and loads the relevant classes.
    */
  case class JobLoaded(loadedDangerousClasses: Set[String])
  extends ContainmentToWorker
  
  /** Tells the containment to abort a job, because it contains 
    * code that should not be executed
    */
  case object AbortJob extends WorkerToContainment

  /** Confirms that the job has been aborted.
    */
  case object JobAborted extends ContainmentToWorker
  
  /** Tells the containment to execute the loaded job
    */
  case object RunJob extends WorkerToContainment
  
  /** Confirms that the job has been started.
    */
  case object JobRunning extends ContainmentToWorker

  /** Tells the worker node manager that this containment has finished
    * executing its code.
    */
  case class Result(res: Any) extends ContainmentToWorker

}