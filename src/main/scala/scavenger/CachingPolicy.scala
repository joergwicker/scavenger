package scavenger

/** A caching policy determines whether a computation
  * should be cached on a particular kind of compute node,
  * and whether it should be backed up on hard drive.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
case class CachingPolicy(
  cacheGlobally: Boolean,
  cacheLocally: Boolean,
  backup: Boolean
)

object CachingPolicy {
  val Nowhere = CachingPolicy(false, false, false)
}
