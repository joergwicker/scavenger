package scavenger
import scavenger.categories.freeccc.{Identity => ArrowIdentity}

/**
 * The trivial algorthihm that does not modify the input.
 * Can be used to pass data around.
 */
case class Identity[X]() extends Algorithm[X, X] {
  def identifier = ArrowIdentity[X]
  def apply(resource: Resource[X]) = resource
}