package scavenger

import scala.concurrent.{Future, ExecutionContext, Await}
import scala.concurrent.duration._

/** This package contains implementation of
  * control flow structures like `if` and `while` with
  * bodies that return a result asynchronously.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
package object controlflow {
  
  def full_async_while[U](
    p: => Future[Boolean]
  )(
    f: => Future[U]
  )(
    implicit exec: ExecutionContext
  ): Future[Unit] = {
    p.flatMap{ 
      if (_) f.flatMap(u => full_async_while(p)(f)) 
      else Future { () }
    }
  }
  
  def async_while[U](
    p: => Boolean
  )(
    f: => Future[U]
  )(
    implicit exec: ExecutionContext
  ): Future[Unit] = {
    if(p) f.flatMap{u => async_while(p)(f)} else Future{()} 
  }
  
  def async_if[R, A <: R, B <: R](
    p: => Future[Boolean]
  )(
    t: => Future[A]
  )(
    f: => Future[B]
  )(
    implicit exec: ExecutionContext
  ): Future[R] = {
    p.flatMap{ if (_) t else f }
  }
  
  // def main(args: Array[String]): Unit = {
  //   def foo(i: Int)(implicit exec: ExecutionContext): Future[Int] =
  //     Future { Thread.sleep(2000); i * 2 }
  //     
  //   def pred(i: Int)(implicit exec: ExecutionContext): Future[Boolean] =
  //     Future { Thread.sleep(1000); i < 10 }
  //   
  //   import scala.concurrent.ExecutionContext.Implicits.global
  //   
  //   var i = 0
  //   val f = full_future_while(pred(i)) {
  //     foo(i) map { x => println(x); i += 1}
  //   }
  //   
  //   Await.result(f, 1 minute)
  // }
}
