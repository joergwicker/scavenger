/**
 * 2015-03-08
 *
 * Formal CCC's with explicit elements and pairs of 
 * elements. All functions are also elements, but not 
 * all elements are functions.
 *
 * Fucking (co/contra)-variance does not work out here, 
 * `Application` seems somehow inherently evil...
 */
sealed trait Elem[+X]
case class ElemAtom[+X](name: String) extends Elem[X] {
  override def toString = name
}
case class Couple[+X, +Y](x: Elem[X], y: Elem[Y]) extends Elem[(X, Y)] {
  override def toString = "(%s,%s)".format(x, y)
}
case class Application[-X, +Y](f: Arrow[X, Y], x: Elem[X])
extends Elem[Y] {
  override def toString = "%s(%s)".format(f, x)
}

sealed trait Arrow[-X, +Y] extends Elem[X => Y] {
  // two methods that must be implemented,
  // `toString` should be overridden
  def apply(x: Elem[X]): Elem[Y]
  protected def composeNonId[W](right: Arrow[W, X]): Arrow[W, Y]

  
  def o[W](other: Arrow[W, X]): Arrow[W, Y] = other match {
    case i: Id[_] => this.asInstanceOf[Arrow[W, Y]]
    case g => composeNonId(g)
  }

  // never use it anyway?
  // def andThen[Z](other: Arrow[Y, Z]): Arrow[X, Z] = other o this
}

case class ArrowAtom[-X, +Y](name: String) extends Arrow[X, Y] {
  override def toString = name
  def apply(x: Elem[X]) = Application(this, x)
  def composeNonId[W](other: Arrow[W, X]) = Composition(this, other)
}

case class Id[X]() extends Arrow[X, X] {
  override def toString = "Id"
  def apply(x: Elem[X]) = x
  def composeNonId[W](other: Arrow[W, X]) = other
}

case class Composition[-X, Y, +Z](second: Arrow[Y, Z], first: Arrow[X, Y])
extends Arrow[X, Z] {
  override def toString = "%s o %s".format(second, first)
  def apply(x: Elem[X]) = second(first(x))
  def composeNonId[W](other: Arrow[W, X]) = second o (first o other)
}

case class Pair[-Dom, +X, +Y](f: Arrow[Dom, X], g: Arrow[Dom, Y]) 
extends Arrow[Dom, (X, Y)] {
  def apply(x: Elem[Dom]) = Couple(f(x), g(x))
  def composeNonId[W](other: Arrow[W, Dom]) = Pair(f o other, g o other)
  override def toString = "<%s,%s>".format(f, g)
}

case class Fst[X, -Y]() extends Arrow[(X, Y), X] {
  override def toString = "Fst"
  def apply(xy: Elem[(X, Y)]) = xy match {
    case Application(f, x) => (this o f)(x)
    case Couple(x, y) => x
    case x => Application(this, x)
  }
  def composeNonId[W](other: Arrow[W, (X, Y)]) = other match {
    case Pair(f, g) => f
    case f => Composition(this, f)
  }
}

case class Snd[-X, Y]() extends Arrow[(X, Y), Y] {
  override def toString = "Snd"
  def apply(xy: Elem[(X, Y)]) = xy match {
    case Application(f, x) => (this o f)(x)
    case Couple(x, y) => y
    case x => Application(this, x)
  }
  def composeNonId[W](other: Arrow[W, (X, Y)]) = other match {
    case Pair(f, g) => g
    case f => Composition(this, f)
  }
}

case class Curry[-A,-B,+Z](f: Arrow[(A, B), Z]) extends Arrow[A, B => Z] {
  override def toString = "lambda(%s)".format(f)
  def apply(a: Elem[A]) = Application(this, a)
  def composeNonId[W](other: Arrow[W, A]) = Composition(this, other)
}

case class Eval[Y, Z]() extends Arrow[(Y => Z, Y), Z] {
  override def toString = "eval"
  def apply(a: Elem[(Y => Z, Y)]) = a match {
    case Application(f, x) => (this o f)(x)
    case Couple(Application(Curry(f), x), y) => f(Couple(x, y))
    case Couple(f: Arrow[Y, Z], x) => f(x)
    case sthElse => Application(this, sthElse)
  }
  def composeNonId[W](other: Arrow[W, (Y => Z, Y)]) = other match {
    case Pair(Composition(Curry(f), x), y) => f o Pair(x, y)
    case sthElse => Composition(this, sthElse)
  }
}

val x = ElemAtom[Int]("x")
val y = ElemAtom[Double]("y")
val e = (Eval[Double, Int]())(Couple(Curry(Fst[Int, Double])(x), y))

println(e)