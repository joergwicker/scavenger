package scavenger.categories.formalccc

sealed trait Elem {
  // two methods that must be implemented,
  // `toString` should be overridden
  def apply(x: Elem): Elem
  protected def composeNonId(right: Elem): Elem
  
  def o(other: Elem): Elem = other match {
    case Id => this
    case g => composeNonId(g)
  }

  protected def ensureNotCouple = this match {
    case x: Couple => throw new CouplesNotFunctions
    case _ => { /* ok */ }
  }
 
  def cross(other: Elem): Elem = {
    this.ensureNotCouple
    other.ensureNotCouple
    Pair(this o Fst, other o Snd)
  }
}

case class Atom(name: String) extends Elem {
  override def toString = name
  def apply(arg: Elem) = Application(this, arg)
  def composeNonId(other: Elem) = Composition(this, other)
}

class CouplesNotFunctions(
  msg: String = "Couples of functions are not functions"
) extends RuntimeException

case class Couple(x: Elem, y: Elem) extends Elem {
  override def toString = "(%s,%s)".format(x, y)
  def apply(arg: Elem) = throw new CouplesNotFunctions
  def composeNonId(other: Elem) = ???
  override def o(other: Elem): Elem = throw new CouplesNotFunctions
}
case class Application(f: Elem, x: Elem) extends Elem {
  override def toString = "%s(%s)".format(f, x)
  def apply(arg: Elem) = Application(this, arg)
  def composeNonId(other: Elem) = Composition(this, other)
}

case object Id extends Elem {
  override def toString = "Id"
  def apply(x: Elem) = x
  def composeNonId(other: Elem) = other
}

case class Composition(second: Elem, first: Elem)
extends Elem {
  override def toString = "%s o %s".format(second, first)
  def apply(x: Elem) = second(first(x))
  def composeNonId(other: Elem) = second o (first o other)
}

case class Pair(f: Elem, g: Elem) 
extends Elem {
  def apply(x: Elem) = Couple(f(x), g(x))
  def composeNonId(other: Elem) = Pair(f o other, g o other)
  override def toString = "<%s,%s>".format(f, g)
}

case object Fst extends Elem {
  override def toString = "Fst"
  def apply(xy: Elem) = xy match {
    case Application(f, x) => (this o f)(x)
    case Couple(x, y) => x
    case x => Application(this, x)
  }
  def composeNonId(other: Elem) = other match {
    case Pair(f, g) => f
    case f => Composition(this, f)
  }
}

case object Snd extends Elem {
  override def toString = "Snd"
  def apply(xy: Elem) = xy match {
    case Application(f, x) => (this o f)(x)
    case Couple(x, y) => y
    case x => Application(this, x)
  }
  def composeNonId(other: Elem) = other match {
    case Pair(f, g) => g
    case f => Composition(this, f)
  }
}

case class Curry(f: Elem) extends Elem {
  override def toString = "lambda(%s)".format(f)
  def apply(x: Elem) = PartialApplication(f, x)
  def composeNonId(other: Elem) = Composition(this, other)
}

case class PartialApplication(f: Elem, x: Elem) extends Elem {
  override def toString = "lambda(%s)(%s)".format(f, x)
  def apply(y: Elem) = f(Couple(x, y))
  def composeNonId(other: Elem) = Composition(this, other)
}

case object Eval extends Elem {
  override def toString = "eval"
  def apply(a: Elem) = a match {
    case Application(f, x) => (this o f)(x)
    case Couple(Application(Curry(f), x), y) => f(Couple(x, y))
    case Couple(f: Elem, x) => f(x)
    case sthElse => Application(this, sthElse)
  }
  def composeNonId(other: Elem) = other match {
    case Pair(Composition(Curry(f), x), y) => f o Pair(x, y)
    case sthElse => Composition(this, sthElse)
  }
}

/* little demo
val x = Atom("x")
val y = Atom("y")
val e = Eval(Couple(Curry(Fst)(x), y))
println(e)

val f = Atom("f")
val lf = Curry(f o Pair(Snd, Fst))(y)(x)
println(lf)

val foo = ((x cross Id) o Pair(Id, y))(f(f)(f(f)))
println(foo)
// */
