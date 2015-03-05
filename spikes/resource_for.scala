import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Foo {

type Identifier[X] = String

trait Context {
  def submit[X](r: Resource[X]): Future[X]
}

class EasyContext extends Context {
  def submit[X](r: Resource[X]): Future[X] = r.compute(this)
}

trait CanApplyTo[-What, -To, +Out] {
  def apply(w: What, t: To): Out
}

case class In[+X](x: X, ctx: Context)

case class ResourceBuilder[+X](from: Resource[X], arrowName: String) {
  def filter(input: In[X] => Boolean) = this /* HACK */
  def map[Y](f: In[X] => Y): Resource[Y] = {
    from.flatMap(arrowName, {case (x, ctx) => Future(f(In(x, ctx))) } )
  }
  def flatMap[Y](f: In[X] => Future[Y]): Resource[Y] = {
    from.flatMap(arrowName, {case (x, ctx) => f(In(x, ctx)) } )
  }
}

trait Resource[+X] { x =>
  def compute(ctx: Context): Future[X]
  def identifier: Identifier[X]

  def flatMap[Y](arrowName: String, f: (X, Context) => Future[Y]): 
  Resource[Y] = {
    new Resource[Y] {
      def identifier = arrowName + " o " + x.identifier
      def compute(c: Context) = {
        c.submit(x).flatMap{ f((_:X), c) }
      }
    }
  }

  def mapSynchronously[Y](arrowName: String, f: (X, Context) => Y) =
    flatMap(arrowName, { case (x, c) => Future{f(x, c)} })

  def mapWithoutContext[Y](arrowName: String, f: X => Future[Y]) = 
    flatMap(arrowName, { case (x, c) => f(x) })

  def map[Y](arrowName: String, f: X => Y) = 
    flatMap(arrowName, { case (x, c) => Future{f(x)} })

  def into(arrowName: String): ResourceBuilder[X] =
    ResourceBuilder(this, arrowName)

  def zip[Y](other: Resource[Y]): Resource[(X, Y)] = {
    new Resource[(X, Y)] {
      def identifier = "(" + x.identifier + "," + other.identifier + ")"
      def compute(c: Context) = {
        c.submit(x).zip(c.submit(other))
      }
    }
  }

  def apply[Y, Z](other: Resource[Y])(implicit cat: CanApplyTo[X, Y, Z]):
    Resource[Z] = new Resource[Z] {
      def identifier = "eval o <%s,%s>".format(x.identifier, other.identifier)
      def compute(c: Context) = {
        for {
          f <- c.submit(x)
          arg <- c.submit(other)
        } yield cat(f, arg)
      }
    }
}

object Resource {
  def apply[X](name: String, x: => X): Resource[X] = {
    new Resource[X] {
      def identifier = name
      def compute(ctx: Context) = Future(x)
    }
  }
}

def main(args: Array[String]): Unit = {
  println("hello, world")
  def block[X](title:String)(f: => X) = {
    println("############ %s ###########".format(title))
    f
    Thread.sleep(3)
  }
  
  val ctx = new EasyContext
  val xRes = Resource("x", 5)
  
  block("The explicit method to transform x") {
    val y = xRes.flatMap("square", { case (i, c) => Future(i * i) })
    println(y.identifier)
    y.compute(ctx).onComplete{ x => println(x) }
  }
  
  block("The desugared-for-method") {
    val y = xRes.into("square").filter { 
      case In(x, ctx) => true
      case _ => false
    }.map {
      case In(x, ctx) => Future(x * x)
    }
    println(y.identifier)
    y.compute(ctx).onComplete{ println }
  }

  block("The for-comprehension method") {
    val y = for {
      In(x, ctx) <- xRes.into("square")
      q <- Future(x * x)
    } yield q
    println(y.identifier)
    y.compute(ctx).onComplete{ println }
  }
  
  println("terminating...")
}

}