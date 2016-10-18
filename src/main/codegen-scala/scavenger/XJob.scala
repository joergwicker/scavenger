package scavenger
import scala.language.higherKinds

/** <<DOC_TOP>>
  *
  * @since 2.3
  * @author Andrey Tyukin
  */
sealed trait <<X>>Job[+X] <<EXTENDS>> {
  <<ZIP_METHODS>>
}
 
case class <<X>>Apply[X, +Y](
  f: <<X>>Algorithm[X, Y], 
  x: <<X>>Job[X]
) extends <<X>>Job[Y]

case class <<X>>Pair[+X, +Y](
  _1: <<X>>Job[X],
  _2: <<X>>Job[Y]
) extends <<X>>Job[(X, Y)] {
}

case class <<X>>Jobs[X, +CC[E] <: scavenger.GenericProduct[E, CC]]
(jobs: CC[<<X>>Job[X]]) extends <<X>>Job[CC[X]] 

// case class <<X>>Value[+X](value: X, identifier: String) extends <<X>>Job[X]
