package scavenger

/**
 * This package contains an implementation of a
 * Scavenger-service based on Akka.
 * 
 * It contains implementation of Master, Worker and Seed
 * actors that exchange messages in order to perform the
 * computation in distributed fashion.
 */
package object backend {
  trait HandshakeMessage
}