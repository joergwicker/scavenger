package scavenger

class SimplificationException(
  computationDescription: String, 
  reason: String
) extends RuntimeException(
  "Cannot rebuild simplified %s, reason: %s".format(
    computationDescription,
    reason
  )
)