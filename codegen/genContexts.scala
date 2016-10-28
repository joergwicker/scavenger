import scala.math.Ordering

/* Define various difficulty levels, and a way to compare them */
sealed trait Difficulty
case object Trivial extends Difficulty
case object Local extends Difficulty
case object Distributed extends Difficulty

val allDifficulties = List(Trivial, Local, Distributed)

/** returns the next lower difficulty, if there is one */
def lower(d: Difficulty): Option[Difficulty] = d match {
  case Distributed => Some(Local)
  case Local => Some(Trivial)
  case Trivial => None
}

def higher(d: Difficulty): Option[Difficulty] = d match {
  case Distributed => None
  case Local => Some(Distributed)
  case Trivial => Some(Local)
}

/** ordering of the difficulties */
implicit val diffOrd: Ordering[Difficulty] = new Ordering[Difficulty] {
  private def idx(d: Difficulty) = d match {
    case Trivial => 0
    case Local => 1
    case Distributed => 2
  }
  def compare(a: Difficulty, b: Difficulty): Int = {
    idx(a) - idx(b)
  }
}
import diffOrd._

/* Text-processing utils: indentation etc. */
def indent(what: String, indentation: String = "  "): String = {
  what.split("\n").map(x => indentation + x).mkString("\n")
}


def contextSource(d: Difficulty): String = {
  "trait %sContext {\n%s\n}\n".format(
    d,
    indent(
      (for (x <- allDifficulties.reverse; if x <= d) yield {
        computationAbstractTypeMember(x, d) +
        algorithmAbstractTypeMember(x, d) +
        computationOps(x, d) +
        algorithmOps(x, d)

        
      }).mkString("\n")
    )
  )
}

def computationAbstractTypeMember(
  x: Difficulty, 
  maxHigher: Difficulty
): String = {
  val filteredHigher = higher(x).filter(_ <= maxHigher)
  "type %sComputation[+X] <: %s%sComputationOps[X]\n".format(
    x, 
    filteredHigher.map(_ + "Computation[X] with ").getOrElse(""),
    x
  )
}

def algorithmAbstractTypeMember(
  x: Difficulty,
  maxHigher: Difficulty
): String = {
  val filteredHigher = higher(x).filter(_ <= maxHigher)
  "type %sAlgorithm[-X, +Y] <: %s%sAlgorithmOps[X, Y]\n".format(
    x,
    filteredHigher.map(_ + "Algorithm[X, Y] with ").getOrElse(""),
    x
  )
}

def computationOps(x: Difficulty, d: Difficulty): String = {
  "\ntrait %sComputationOps[+X] {\n  self: %sComputation[X] =>\n%s\n}\n".format(
    x, // trait name
    x, // self-type
    indent(
      "def zip[Y](y: %sComputation[Y]): %sComputation[Y]\n".format(x, x)
    )
  )
}

def algorithmOps(opsDiff: Difficulty, ctxDiff: Difficulty): String = {
  "\ntrait %sAlgorithmOps[-X, +Y] {\n  self: %sAlgorithm[X, Y] =>\n%s\n}\n"
    .format(
    opsDiff,
    opsDiff,
    indent(
      algoApplyMethod(opsDiff) + 
      algoAndThenMethod(opsDiff) + 
      algoOMethod(opsDiff) + 
      algoZipMethod(opsDiff) +
      algoCrossMethod(opsDiff)
      // partials
    )
  )
}

def algoApplyMethod(d: Difficulty): String = {
  "def apply(c: %sComputation[X]): %sComputation[Y]\n".format(d, d)
}

def algoAndThenMethod(d: Difficulty): String = {
  "def andThen[Z](a: %sAlgorithm[Y, Z]): %sAlgorithm[X, Z]\n".format(d, d)
}

def algoOMethod(d: Difficulty): String = {
  "def o[W](frst: %sAlgorithm[W, X]): %sAlgorithm[W, Y] =\n".format(d, d) + 
  "  frst andThen this\n"
}

def algoZipMethod(d: Difficulty): String = {
  "def zip[Z, V <: X](a: %sAlgorithm[V, Z]): %sAlgorithm[V, (Y, Z)]\n".
    format(d, d)
}

def algoCrossMethod(d: Difficulty): String = {
  ("def cross[A, B](a: %sAlgorithm[A, B]): %sAlgorithm[(X, A), (Y, B)] = \n" +
  "  (this o fst[X, A]()) zip (a o snd[X, A]())\n").format(d, d)
}

println("// This code is machine-generated")
println("// Do not change it manually: all changes will be overridden")
println("")
println("import scala.concurrent.{ExecutionContext, Future}")
println("import scala.language.higherKinds")
println("")

for (d <- allDifficulties) {
  println(contextSource(d))
}