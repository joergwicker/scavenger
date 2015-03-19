package scavenger.categories

/** 2015-03-08
  *
  * Formal Cartesian Closed Category without type annotations.
  *
  * It does not distinguish between elements and functions,
  * but it throws some exceptions if one attempts to form
  * expressions that are guaranteed to never make sense,
  * like `(f,g)(x)` (2-Tuples of functions are not functions).
  *
  * Seems that this is in principle an implementation
  * of the Categorical Abstract Machine (for programs
  * given as abstract syntax trees)
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
package object formalccc {

}