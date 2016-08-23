package scavenger.backend.worker

import akka.actor._
import akka.contrib.pattern.ReliableProxy
import scala.concurrent.duration._
import scala.language.postfixOps
import scavenger.backend._
import scavenger.backend.seed.Seed.MasterRef
import scavenger.backend.seed.Seed.MasterShutdown

/** Helps to join the master node.
  *
  * This mixin implements actor behavior that is
  * useful at the initial stage when the connection to
  * the `Master` node has not yet been established.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
trait MasterJoin extends Actor 
with ActorLogging
with Remindable 
with SeedJoin {

  import MasterJoin._
  import context.dispatcher 

  // reference to master
  private var _master: ActorRef = _
  
  /** We use a reliably proxy to the master in order to
    * communicate results, since the results are rather
    * expensive to obtain (would be bad if we lose them
    * without a good reason)
    */
  private var _masterProxy: ActorRef = _ 
  
  private def master_=(ref: ActorRef): Unit = {
    _master = ref
    _masterProxy = context.actorOf(
      ReliableProxy.props(_master.path, 10 seconds)
    )
  }
  
  protected[backend] def master = _master
  protected[backend] def masterProxy = _masterProxy

  /** Establishes connection to the master node.
    * Assumes that the connection to seed node has already been established.
    * Requires a `Reminder` that initiates the connection process.
    */
  def connectingToMaster(
    seedMsg: HandshakeMessage,
    handshakeToMaster: HandshakeMessage,
    nextBehavior: Receive
  ): Receive = ({

    case r: Reminder if(isRelevant(r)) => {
      if (master == null) {
        seedRef ! seedMsg
        remindMyself(30, "trying to resolve master")
      }
    }
    
    // seed should send us the `MasterRef` sooner or later.
    // After this we can send a handshake message to the master,
    // and switch to the next behavior
    case MasterRef(ref) => {
      master = ref
      master ! handshakeToMaster
      log.info(
        "Received Master-ActorRef from seed; Sent handshake to the master; switching into normal operation mode"
      )
      context.become(nextBehavior orElse handleHandshakeRemnants)
    }
    
    // after master shut down, seed will send a message
    case MasterShutdown() => {
      master = null
      log.info(
        "Master shut down, waiting for another master!"
      )
      context.become(handleHandshakeRemnants)
    }
  }: Receive)

  def handleHandshakeRemnants: Receive = ({
    case MasterRef(_) => { /* don't need it anymore, ignore */ }
  }: Receive)
}

object MasterJoin {

}
