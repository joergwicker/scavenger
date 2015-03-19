package scavenger.backend.worker

import scavenger.CachingPolicy
import scavenger.backend.Cache

/** Worker-specific cache
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
trait WorkerCache extends Cache {
  protected def shouldBeCachedHere(cachingPolicy: CachingPolicy): Boolean = {
    cachingPolicy.cacheLocally
  }
}
