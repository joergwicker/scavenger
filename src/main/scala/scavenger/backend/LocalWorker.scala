package scavenger.backend

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import scavenger.{Context, Resource}
import scavenger.backend._

/**
 * A simple actor that can be launched on either 
 * Master and Worker nodes. 
 * 
 * The purpose of this kind of actors is to keep 
 * the node-manager actor responsive and isolate it
 * from eventual errors.
 */
class LocalWorker(val ctx: Context) extends Actor {

  import context.dispatcher

  def receive = ({
    case InternalJob(r) => {
      r.compute(ctx).map{
        x => InternalResult(r.identifier, x)
      } pipeTo context.parent
    }
  }: Receive)
}

object LocalWorker {
  def props(ctx: Context): Props = Props(classOf[LocalWorker], ctx)
}