package hotswapWorker.protocol
package master_worker {

  sealed trait MasterToWorker extends Direction
  sealed trait WorkerToMaster extends Direction

  /** Initialization message for worker nodes that contains the list of 
    * prefixes that determine the set of "dangerous" classes.
    */
  case class Initialize(dangerousClassesPrefixes: List[String])
  extends MasterToWorker

  /** Job serialized as byte array
    *
    */
  case class SerializedJob(bytes: Array[Byte])
  extends MasterToWorker

  /** Asks the worker to blacklist the class with the specified name.
    * 
    * This has the effect that jobs that contain the blacklisted class
    * are not started. However, it does not attempt those jobs with
    * the blacklisted class, which are already running.
    */
  case class Blacklist(className: String)
  extends MasterToWorker

  /** Asks the worker to remove a class from the blacklist.
    * That is, the presence of this class in the job will no longer
    * prevent the `Worker` from executing the job.
    */
  case class UnBlacklist(className: String)
  extends MasterToWorker

  /** Asks the worker to attempt to stop all `Containment`s that have
    * loaded the class with the specified class name.
    *
    * If the attempt fails, the worker node can simply ignore it.
    * No response is expected by the worker node.
    */
  case class TryToStop(className: String)
  extends MasterToWorker
}
