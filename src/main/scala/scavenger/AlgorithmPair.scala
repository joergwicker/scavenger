package scavenger

import scavenger.categories.formalccc

/** Pair of algorithms with same domain.
  *
  * Result of zipping two algorithms of types `[D, X]` and `[D, Y]` together.
  *
  * @constructor Constructs `<f,g>` from arguments `f` and `g`
  * @param f first component
  * @param g second component
  *
  * @author Andrey Tyukin
  * @since 2.1
  */
case class AlgorithmPair[D, X, Y](
  f: Algorithm[D, X], 
  g: Algorithm[D, Y]
) extends Algorithm[D, (X, Y)] {
  def identifier = 
    formalccc.Pair(f.identifier, g.identifier)
  def apply(r: Computation[D]) = ComputationPair(f(r), g(r))
}
