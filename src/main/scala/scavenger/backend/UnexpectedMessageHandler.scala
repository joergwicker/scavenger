package scavenger.backend

import akka.actor._
import scavenger._

trait UnexpectedMessageHandler extends Actor with ActorLogging {
  def handleUnexpectedMessages: Receive = ({
    case unexpectedMessage => {
      unexpectedMessage match {
        case akka.actor.Status.Failure(exc) => {
          val sw = new java.io.StringWriter()
          val pw = new java.io.PrintWriter(sw)
          exc.printStackTrace(pw)
          val stackTrace = sw.toString()
          log.error("Exception caught in Worker: " + 
            exc.getMessage + "\n" + stackTrace
          )
        }
        case somethingElse => {
          log.warning(
           "Received something unexpected: " + unexpectedMessage + " " + 
           "The class of this thing is: " + unexpectedMessage.getClass + " "
          )
        }
      }
    }
  }: Receive)
}
