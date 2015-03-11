package scavenger.backend.master

import scavenger.CachingPolicy
import scavenger.backend.Cache

trait MasterCache extends Cache {
  protected def shouldBeCachedHere(cachingPolicy: CachingPolicy): Boolean = {
    cachingPolicy.cacheGlobally || cachingPolicy.backup
  }
}
