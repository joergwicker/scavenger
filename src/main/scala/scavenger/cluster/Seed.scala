package scavenger.cluster

import akka.actor.Actor
import Seed._
import akka.actor.ActorRef
import akka.actor.Terminated
import akka.actor.ActorLogging
import scavenger.cluster.Master.MasterHere
import scavenger.cluster.Worker.WorkerHere
import akka.actor.Props

/**
 * This actor is responsible for awaiting the backup server and 
 * the master, then helping the workers to find the master.
 * Basically, it just tries to connect everyone with everyone else.
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
    
    case Terminated(worker) =>
      workers = workers.filterNot(_ == worker)
  }
}

object Seed {
  
  def props = Props[Seed]
  
  private[cluster] case class MasterRef(master: ActorRef)
}