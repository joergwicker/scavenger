package scavenger

import scavenger.categories.freeccc

/**
 * Pair of algorithms with same domain.
 * Result of zipping two algorithms together.
 */
case class AlgorithmPair[D, X, Y](
  f: Algorithm[D, X], 
  g: Algorithm[D, Y]
) extends Algorithm[D, (X, Y)] {
  def identifier = 
    freeccc.Pair(f.identifier, g.identifier)
  def apply(r: Resource[D]) = ResourcePair(f(r), g(r))
}