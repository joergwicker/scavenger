package scavenger.algebra.categories

import scala.concurrent.{Future, ExecutionContext}
import scala.language.higherKinds

/** Universally quantified asynchronous polymorphic function
  * which can be evaluated in some context.
  * Roughly speaking, `forall X. (F[X], Ctx) => Future[G[X]]`
  *
  * Essentially all "sufficiently reasonable" 
  * implementations of such mappings between functors 
  * `F` and `G` are natural transformations, except that
  * the mappings require an additional context,
  * and are asynchronous rather than synchronous.
  *
  * Similar in spirit to the "natural transformation" `~>[F[_], G[_]]` in scalaz
  */
trait FutNat[-Ctx, -F[_], +G[_]] {
  def apply[X](arg: F[X])(implicit ctx: Ctx): Future[G[X]]
}