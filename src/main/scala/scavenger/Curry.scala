/*
package scavenger

import scala.concurrent.Future
import scavenger.categories.freeccc

case class Curry[-X, -Y, +Z](alg: Algorithm[(X, Y), Z]) 
extends Algorithm[X, (Y => Z)] {
  def identifier = freeccc.Curry[X, Y, Z]
  def apply(r: Resource[X]): Resource[Y => Z] = new Resource[Y => Z] {
    def identifier = 
    def compute(c: Context): Future[Y => Z] = {
       for (x <- c.submit(r)) yield 

    }
  }
}
*/