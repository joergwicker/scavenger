package scavenger
import scavenger.categories.formalccc

/** The trivial algorithm that does not modify the input.
  *
  * Can be used to pass data around.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
case class Identity[X]() extends Algorithm[X, X] {
  def identifier = formalccc.Id
  def apply(computation: Computation[X]) = computation
}
