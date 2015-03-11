package scavenger.backend.worker

import scavenger.CachingPolicy
import scavenger.backend.Cache

trait WorkerCache extends Cache {
  protected def shouldBeCachedHere(cachingPolicy: CachingPolicy): Boolean = {
    cachingPolicy.cacheLocally
  }
}
