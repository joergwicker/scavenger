package scavenger.backend.worker
import akka.actor._
import scala.collection.mutable.{HashSet, HashMap}
import scavenger.backend.worker.protocol._

/** Part of the `Worker` node functionality that
  * is responsible for managing multiple containments.
  *
  * Spawns multiple `Containment`s, in which it can run 
  * potentially instable code.
  */
trait WorkerContainmentManager
extends Actor 
with ActorLogging {

  // TODO: remove magic number
  def numContainments = 10

  /** Prefixes of classes that must be loaded separately */
  private var isolatedClassesPrefixes: List[String] = Nil

  /** Set of all containments */
  private var containments: HashSet[ActorRef] = HashSet.empty

  /** Set of containments that are currently not occupied by a job */
  private var freeContainments: HashSet[ActorRef] = HashSet.empty

  /** For each containment, a set of classnames that have been loaded by
    * the containment.
    */
  private var loadedIsolatedClasses: HashMap[ActorRef, Set[String]] =
    HashMap.empty

  /** Set of classes that are know to be broken.
    * If a job contains a class from this list, it should not
    * be started.
    */
  private val blacklist: HashSet[String] = HashSet.empty

  import protocol.master_worker._
  import protocol.worker_containment._

  def receiveMasterToWorker = ({
    case Initialize(prefixes) => {
      isolatedClassesPrefixes = prefixes
      // create pool of containments, tell each containment to 
      // load the dangerous classes by an isolating classloader
      containments = HashSet.empty[ActorRef] ++ (
        for (i <- 1 to numContainments) yield {
          context.actorOf(Containment.props(prefixes))
        }
      )

      // when the node is initialized, all containments are not occupied
      freeContainments = HashSet.empty ++ containments

      // at the start, no containment has loaded any bytecode
      loadedIsolatedClasses = HashMap.empty[ActorRef, Set[String]] ++ 
        (for (c <- containments) yield {
          (c, Set[String]())
        })
    }

    case SerializedJob(bytes) => {
      /** TODO: this is good enough to demonstrate the point
        * with class-reloading, but
        * not good enough for actual use.
        */
      if (freeContainments.isEmpty) {
        throw new Error("")
      } else {
        val c = freeContainments.head
        freeContainments.remove(c)
        c ! LoadJob(bytes)
        freeContainments -= c
      }
    }

    case Blacklist(badClass) => {
      blacklist += badClass
    }

    case UnBlacklist(goodClass) => {
      blacklist.remove(goodClass)
    }

    case TryToStop(badClass: String) => {
      for ((containment, dangerousClasses) <- loadedIsolatedClasses) {
        if (dangerousClasses.contains(badClass)) {
          context.stop(containment)
        }
      }
    }
  } : ReceiveAll[MasterToWorker])

  def receiveContainmentToWorker = ({
    case JobLoaded(dangerousClasses) => {
      loadedIsolatedClasses(sender) = dangerousClasses
      if (blacklist.exists(dangerousClasses.contains)) {
        sender ! AbortJob
        freeContainments -= sender
      } else {
        sender ! RunJob
      }
    }
    case JobAborted => ??? // TODO
    case JobRunning => ??? // TODO
    case Result(res) => {
      freeContainments += sender
    }
  } : ReceiveAll[ContainmentToWorker])

  def handleUnexpected = ({
    case sthUnexpected => log.warning(
      "Unexpected message of type " + sthUnexpected.getClass + 
      " with value: " + sthUnexpected
    )
  } : Receive)

  def receive = ({
    case x: MasterToWorker => receiveMasterToWorker(x)
    case x: ContainmentToWorker => receiveContainmentToWorker(x)
  }: Receive) orElse 
  handleUnexpected
}

/* TODO: CRUFT?
object WorkerContainmentManager {
  def props(n: Int) = Props(classOf[WorkerContainmentManager], n)
}
*/
