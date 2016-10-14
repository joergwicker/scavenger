package scavenger.algebra.categories
import scala.language.higherKinds

trait FormalCCC {
  
  type Arrow[-X, +Y]
  type Elem[+X]
  
  def atomicElem[X](name: String): Elem[X]
  def pair[X, Y](x: Elem[X], y: Elem[Y]): Elem[(X, Y)]
  def apply[X, Y](f: Arrow[X, Y], x: Elem[X]): Elem[Y]
  /*
  def elements[X, M[+X] <: TraversableOnce[X]]
    (es: M[X])
    (implicit cbf: CanBuildFrom[M[X], Elem[X], M[Elem[X]]]): Elem[M[X]]
  */
  
  def atomicArrow[X, Y](name: String): Arrow[X, Y]
  def id[X]: Arrow[X, X]
  def compose[X, Y, Z](second: Arrow[Y, Z], first: Arrow[X, Y]): Arrow[X, Z]


  def decideEquality[X](a: Elem[X], b: Elem[X]): Boolean
}