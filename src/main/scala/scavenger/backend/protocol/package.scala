package scavenger.backend.worker

/** This package defines sets of messages that can be exchanged between
  * various components of the system.
  */
package object protocol {

  /** Total function from `A` to unit.
    *
    * Expected to be used as part of `Receive` like this:
    * 
    * {{{
      def f : ReceiveAll[X] = ???
      def receive = ({
        case x: X => f(x)
        ...
      } : Receive)
    }}}
    *
    * This forces the `receive` to handle all messages of type `X`,
    * and the compiler can generate sensible warnings if we forget any
    * cases in the implementation of `f`.
    */
  type ReceiveAll[-A] = (A => Unit)
}