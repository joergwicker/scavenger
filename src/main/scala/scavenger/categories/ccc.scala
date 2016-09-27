/*
sealed trait Mor[-X, +Y] {
  def eval: Value[-X, +Y]
}

case class Id[X]() extends Mor[X, X] {
  def eval = Vid[X]()
}

case class Comp[-X, Y, +Z](g: Mor[Y, Z], f: Mor[X, Y]) extends Mor[X, Z] {
  def eval = g.eval(f.eval) // calls Value.apply()
}

sealed trait Value[-X, +Y] {
  def reify: Mor[-X, +Y]
  def apply[W](v: Value[W, X]): Value[W, Y] = {
    case v: VId => this.asInstanceOf[Value[X, W]]
    case notId => applyToNonId(notId)
  }
  protected def applyToNonId[W](v: Value[W, X]): Value[W, Y]
}

case class VId[X]() extends Value[X, X] {
  def reify = Id[X]()
  protected def applyToNonId[W](v: Value[W, X]) = v
}

sealed trait Neutral[-X, +Y] extends Value[X, Y]

case class VComp[-X, Y, +Z](neut: VNeutral[Y, Z], arg: Value[X, Y]) 
extends Value[X, Z] {
  def reify = Comp(neut.reify, arg.reify)
  protected def applyToNonId[W](v: Value[W, X]) = 
    VComp()
}

case class Prod[-X,+A,+B](fst: Mor[X, A], snd: Mor[X, B]) 
extends Mor[X, (A, B)] {
  def eval = VProd
}
case class Fst[A, -B]() extends Mor[(A, B), A]
case class Snd[-A, B]() extends Mor[(A, B), B]
case class Eval[Y, +Z]() extends Mor[(Y => Z, Y), Z]
case class Veval[Y, +Z]() extends Value[Y, Z]
case class Lam[A, B, Z](f: Mor[(A, B), Z]) extends Mor[A, B => Z]

def simp[-X, +Y]
*/