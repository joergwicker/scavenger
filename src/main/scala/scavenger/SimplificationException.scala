package scavenger

// TODO: what's that good for? is it used anywhere?
/** An exception thrown whenever simplification of a computation fails.
  *
  */
class SimplificationException(
  computationDescription: String, 
  reason: String
) extends RuntimeException(
  "Cannot rebuild simplified %s, reason: %s".format(
    computationDescription,
    reason
  )
)
