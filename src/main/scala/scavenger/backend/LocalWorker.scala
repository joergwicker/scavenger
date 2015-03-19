package scavenger.backend

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import scavenger.{Context, Computation}
import scavenger.backend._
import scavenger.categories.formalccc

/** A simple actor that can be launched on either
  * Master and Worker nodes.
  *
  * The purpose of this kind of actors is to keep
  * the node-manager actor responsive and isolate it
  * from eventual errors.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
class LocalWorker(val ctx: Context) extends Actor {

  import context.dispatcher
  import LocalWorker._
  
  def receive = ({
    case LocalJob(label, r) => {
      r.compute(ctx).map{
        x => LocalResult(label, x)
      } pipeTo context.parent
    }
  }: Receive)
}

object LocalWorker {
  def props(ctx: Context): Props = Props(classOf[LocalWorker], ctx)
  private[backend] case class LocalJob(label: InternalLabel, r: Computation[Any])
  private[backend] case class LocalResult(label: InternalLabel, x: Any)
}
