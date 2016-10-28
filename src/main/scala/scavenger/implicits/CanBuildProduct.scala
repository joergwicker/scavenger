package scavenger.implicits

/** 
 * Instances of this type are evidence that `Prod` is isomorphic to
 * the product of `A` and `B`
 */
trait CanBuildProduct[-A, -B, +Prod] {
  def apply(a: Computation[A], b: Computation[B]): Computation[Prod]
  def split(ab: Computation[Prod]):(Computation[A], Computation[B])
}

