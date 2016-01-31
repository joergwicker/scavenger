package scavenger.backend.seed

import akka.actor.Actor
import Seed._
import akka.actor.ActorRef
import akka.actor.Terminated
import akka.actor.ActorLogging
import scavenger.backend.master.Master.MasterHere
import scavenger.backend.worker.Worker.WorkerHere
import akka.actor.Props

/** This actor helps everyone else to establish connections.
  *
  * It waits for the master, then helps the workers to find the master.
  * Basically, it just tries to connect everyone with everyone else.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
class Seed extends Actor with ActorLogging {
  
  var master: Option[ActorRef] = None
  var workers: List[ActorRef] = List.empty[ActorRef]
  
  def receive = {
    
    case MasterHere => {
      log.info("Master registered at seed node (master = {})", sender)
      master = Some(sender)
      context.watch(sender)
      for (w <- workers) w ! MasterRef(sender)
    }
    
    case WorkerHere => 
      log.info("Worker registered at seed node (worker = {})", sender)
      for (m <- master) sender ! MasterRef(m)
      context.watch(sender)
      workers ::= sender
    
    case Terminated(term) =>
      log.info("Actor = {} terminated!", term)
      if( workers.contains(term)){
        log.info("Identified {} as Worker!", term)
        workers = workers.filterNot(_ == term)
      }
      if( Some(term) == master){
        log.info("Identified {} as Master!", term)
        master = None
        for (w <- workers) w ! MasterShutdown()
      }
      log.info("My Master is {}", master)
  }
}

object Seed {
  
  def props = Props[Seed]
  
  private[backend] case class MasterRef(master: ActorRef)
  private[backend] case class MasterShutdown()
}
