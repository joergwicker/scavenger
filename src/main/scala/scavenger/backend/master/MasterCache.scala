package scavenger.backend.master

import scavenger.CachingPolicy
import scavenger.backend.Cache

/** Master-specific cache.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
trait MasterCache extends Cache {
  
  /** Caches stuff that should be cached globally or backed up */
  protected def shouldBeCachedHere(cachingPolicy: CachingPolicy): Boolean = {
    cachingPolicy.cacheGlobally || cachingPolicy.backup
  }
}
