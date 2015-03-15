import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.HashMap

// vastly simplified model of Scavenger's 
// `ReactiveContext` implementation
class Context {
  // simple cache that maps job ids to futures
  private val cache = new HashMap[String, Future[Unit]]
  def submit(job: Computation): Future[Unit] = {
    if (cache.contains(job.id)) { 
      cache(job.id) // return cached result
    } else {
      val f = job.compute(this) // start computation otherwise
      cache(job.id) = f         // put the future into cache
      f                         // return future
    }
  }
}

// interface for simple computations that always return a `Unit`
trait Computation { 
  def id: String
  def compute(ctx: Context): Future[Unit] 
}

// Leaf node of the dependency graph of computations
case class Simple(id: String) extends Computation { 
  // Returns a future that is fulfilled as soon as the id is printed
  def compute(ctx: Context) = Future {
    Thread.sleep((new scala.util.Random ).nextInt(100))
    printf("%s ", id)
  }
}

// Inner node that has two dependencies
case class Complex(id: String, dependencies: List[Computation]) 
extends Computation {
  // submits all dependencies, waits until they are computed,
  // then prints its own `id`
  def compute(ctx: Context) = {
    val subtasks = for (d <- dependencies) yield ctx.submit(d)
    for (allDone <- Future.sequence(subtasks)) yield printf("%s ", id)
  }
}

// little dependency graph
val a = Simple("a")
val b = Simple("b")
val c = Simple("c")
val d = Simple("d")
val e = Simple("e")
val f = Complex("f", List(b, c))
val g = Complex("g", List(a, f))
val h = Complex("h", List(f, d))
val i = Complex("i", List(g, h))
val j = Complex("j", List(h, e))
val k = Complex("k", List(i, j))

// try to perform the computation `k` a few times,
// observe that the order of execution of tasks `a`,...,`k`
// is not deterministic
for (t <- 0 until 10) {
  val ctx = new Context
  val res = ctx.submit(k)
  Await.result(res, 10 seconds)
  println()
}