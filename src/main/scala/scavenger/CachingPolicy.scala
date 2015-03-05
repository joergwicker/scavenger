package scavenger

/**
 * A caching policy determines whether a resource
 * should be cached on a particular kind of compute node,
 * and whether it should be backed up on hard drive.
 */
case class CachingPolicy(
  cacheOnWorker: Boolean,
  cacheOnMaster: Boolean,
  backup: Boolean
)

object CachingPolicy {
  val Nowhere = CachingPolicy(false, false, false)
}