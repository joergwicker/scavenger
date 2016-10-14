package scavenger

sealed trait <<x>>Job[A] {
  def zip(other: <<x>>Job[B]): <<x>>Job[(A, B)] = <<x>>Pair(this, other)
}

case class <<x>>Pair(a: <<x>>Job[A], b: <<x>>Job[B]) extends <<x>>Job[(A, B)]
case class <<x>>Jobs[A, +CC[E] <: scavenger.GenericProduct[E, CC]]
  (jobs: CC[<<x>>Job[A]]) extends <<x>>Job[CC[X]]
