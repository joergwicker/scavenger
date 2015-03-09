package scavenger

class SimplificationException(
  resourceDescription: String, 
  reason: String
) extends RuntimeException(
  "Cannot rebuild simplified %s, reason: %s".format(
    resourceDescription,
    reason
  )
)