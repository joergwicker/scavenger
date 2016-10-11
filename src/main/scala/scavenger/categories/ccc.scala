/*
sealed trait Mor[-X, +Y] {
  def eval: OldValue[-X, +Y]
}

case class Id[X]() extends Mor[X, X] {
  def eval = Vid[X]()
}

case class Comp[-X, Y, +Z](g: Mor[Y, Z], f: Mor[X, Y]) extends Mor[X, Z] {
  def eval = g.eval(f.eval) // calls OldValue.apply()
}

sealed trait OldValue[-X, +Y] {
  def reify: Mor[-X, +Y]
  def apply[W](v: OldValue[W, X]): OldValue[W, Y] = {
    case v: VId => this.asInstanceOf[Value[X, W]]
    case notId => applyToNonId(notId)
  }
  protected def applyToNonId[W](v: OldValue[W, X]): OldValue[W, Y]
}

case class VId[X]() extends OldValue[X, X] {
  def reify = Id[X]()
  protected def applyToNonId[W](v: OldValue[W, X]) = v
}

sealed trait Neutral[-X, +Y] extends OldValue[X, Y]

case class VComp[-X, Y, +Z](neut: VNeutral[Y, Z], arg: OldValue[X, Y]) 
extends OldValue[X, Z] {
  def reify = Comp(neut.reify, arg.reify)
  protected def applyToNonId[W](v: OldValue[W, X]) = 
    VComp()
}

case class Prod[-X,+A,+B](fst: Mor[X, A], snd: Mor[X, B]) 
extends Mor[X, (A, B)] {
  def eval = VProd
}
case class Fst[A, -B]() extends Mor[(A, B), A]
case class Snd[-A, B]() extends Mor[(A, B), B]
case class Eval[Y, +Z]() extends Mor[(Y => Z, Y), Z]
case class Veval[Y, +Z]() extends OldValue[Y, Z]
case class Lam[A, B, Z](f: Mor[(A, B), Z]) extends Mor[A, B => Z]

def simp[-X, +Y]
*/