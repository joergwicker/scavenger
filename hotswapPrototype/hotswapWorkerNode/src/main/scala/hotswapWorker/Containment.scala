package hotswapWorker

import akka.actor._
import hotswapWorker.protocol._
import java.net.URL

/** An actor spawned by the worker node which can contain "dangerous" 
  * code (i.e. code that is likely to crash / code that one likely 
  * wants to reload). 
  *
  * Every `Containment` has its own `IsolatingClassLoader`.
  * During its lifecycle, a containment can process multiple jobs 
  * (one job at a time).
  * For each deserialized job, it reports to the `Worker`-actor 
  * what "dangerous" classes it had to load for the job. 
  * The `Worker` node can then decide whether to run or to abort the job.
  * If the `Worker` decides that a containment is contaminated, it can send 
  * a `PoisonPill` to the `Containment` thereby asking it to stop.
  *
  * @since 2.2
  * @author Andrey Tyukin
  * 
  * @constructor creates a new `Containment` with the set of "dangerous"
  *   classes specified by prefixes
  * @param isolatedClassPrefixes prefixes that determine the set
  *   of classes that should be loaded separately.
  */
class Containment private[hotswapWorker](
  isolatedClassPrefixes: List[String],
  extraClasspathURLs: List[URL]
) extends Actor { self =>

  /** Default constructor (without `extraClasspathURLs` used only for tests). */
  def this(isolatedClassPrefixes: List[String]) {
    this(isolatedClassPrefixes, Nil)
  }

  /** The `IsolatedClassLoader` used by this containment for 
    * deserialization of incoming jobs.
    */
  private val isolatingClassLoader = new IsolatingClassLoader(
    self.getClass.getClassLoader,
    isolatedClassPrefixes,
    extraClasspathURLs
  )

  /** should be non-empty after loading and during running.
    * should be empty before any job is assigned, and after a loaded job
    * has been aborted.
    */
  private var job: Option[Job[Any]] = None

  import protocol.worker_containment._

  /** Handles all messages that come from the `Worker` 
    * (`Worker` is the worker-node supervisor actor)
    */
  def receiveWorkerToContainment = ({

    case LoadJob(bytes) => {
      val bais = new java.io.ByteArrayInputStream(bytes)
      val icois = new IsolatingObjectInputStream(bais, isolatingClassLoader)
      job = Some(icois.readObject().asInstanceOf[Job[Any]])

      // notice: we send the list of isolated classes created by icois,
      // not by the classloader. The classloader might still contain 
      // "dangerous" code, but it might be irrelevant, because it does
      // not occur in this job.
      sender ! JobLoaded(icois.loadedIsolatedClasses)
    }

    case AbortJob => {
      job match {
        case Some(j) => {
          job = None
          sender ! JobAborted
        }
        case None => throw new IllegalStateException(
          "Cannot abort a job: no job assigned."
        )
      }
    }

    case RunJob => {
      job match {
        case Some(j) => {
          sender ! JobRunning
          sender ! Result(j.doJob())
        }
        case None => throw new IllegalStateException(
          "Cannot run a job: no job assigned."
        )
      }
    }
  }: ReceiveAll[WorkerToContainment])

  def receive = ({
    case x : WorkerToContainment => receiveWorkerToContainment(x)
    case sthElse => throw new IllegalArgumentException(
      "Containment received an unexpected message: " + sthElse + "\n" +
      "current state of the containment: \n" + 
      (if (job.isEmpty) "idle" else "A job has been loaded") + "\n" +
      "Loaded isolated classes: \n" + 
      isolatingClassLoader.
        loadedIsolatedClasses.map{s => "  " + s}.mkString("\n")
    )
  }: Receive)
}

object Containment {
  def props(prefixes: List[String]): Props = 
    Props(classOf[Containment], prefixes, List())

  /** Creates a `Props` that passes the `extraURLs` to the
    * `IsolatingClassLoader` of the `Containment`.
    * Should be used only for testing.
    */
  private[hotswapWorker] def props(
    prefixes: List[String],
    extraURLs: List[URL]
  ): Props = 
    Props(classOf[Containment], prefixes, extraURLs)
}