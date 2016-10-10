package scavenger.util

/** Replaces equality by referential equality,
  * and hash code by identity hash code.
  *
  * This is used for estimating the size of
  * uncompressed and compressed representations of job-graphs.
  * For serialization, it is irrelevant whether some object 
  * thinks that it `equals` to another object: if the two
  * equal objects are different instances, they will be
  * serialized separately, and therefore occupy twice as
  * much memory. In order to get correct estimates for
  * sizes of partially compressed job-graphs, we have
  * to treat each instance separately.
  *
  * @since 2.3
  * @author Andrey Tyukin
  */
private[scavenger] class Instance(wrapped: AnyRef) {
  override def equals(obj: Any): Boolean = obj match {
    case anyRef: AnyRef => anyRef eq wrapped
    case _ => false
  }
  override def hashCode(): Int = java.lang.System.identityHashCode(wrapped)
}