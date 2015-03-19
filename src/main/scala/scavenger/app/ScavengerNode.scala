package scavenger.app

import akka.actor.{ActorPath, ActorSystem}
import java.io.File
import com.typesafe.config._

/**
 * Trait for programs with a `main`-function,
 * loads configuration, creates actor system, starts actors.
 */
private[app] trait ScavengerNode {

  private var actorSystem: ActorSystem = _

  /**
   * Enables each type of node to provide a slightly different
   * version of the configuration that is used to create the
   * `ActorSystem`.
   */
  def extractNodeConfig(generalConfig: Config): Config

  /**
   * Depending on the type of node, this method has to 
   * spawn a master/worker/seed actor in the `system`.
   * 
   * The configuration is put together from the application configuration file
   * and `reference.conf` in `resources` folder of Scavenger.
   */
  private[app] def initializeActors(
    system: ActorSystem, 
    nodeSpecificConfig: Config
  ): Unit


  /**
   * This method composes `ActorPath` of the seed node from various 
   * entries in the specified configuration.
   * 
   * It makes use of the entries following entries in the `seed`-subconfig:
   *
   * {{{
   * scavenger {
   *   seed {
   *     akka.remote.netty.tcp.hostname = "123.456.78.90"
   *     akka.remote.netty.tcp.port = "55555"
   *   }
   * }
   * }}}
   */
  def extractSeedPath(generalConfig: Config): ActorPath = {
    val seedConfig = generalConfig.getConfig("seed")
    val seedIp = seedConfig.getString("akka.remote.netty.tcp.hostname")
    val seedPort = seedConfig.getString("akka.remote.netty.tcp.port")
    val seedPathStr = s"akka.tcp://scavenger@${seedIp}:${seedPort}/user/seed"
    ActorPath.fromString(seedPathStr)
  }

  /**
   * Loads configuration files and initializes an
   * actor system, together with the right set of actors.
   *
   * The configuration files are loaded from the following pathes:
   *
   * 1) /src/main/resources/reference.conf 
   *   contains the default configuration for the framework. Works only
   *   for `LocalScavengerApplication`, because it does not contain an
   *   id of the seed node.
   *   This file is packaged inside the jar, and is therefore not accessible.
   *
   * 2) Application-specific `scavenger.conf` file, that 
   *    should be adjusted by the user. In particularly,
   *    IP and port of the seed node, as well as the directory for
   *    persisted intermediate results should be specified.
   * 
   * 3) Environment variables and java arguments can be used to override 
   *    certain settings, but we prefer to use the configuration files.
   *    
   */
  def scavengerInit(): Unit = {
    val referenceConf = ConfigFactory.load()
    val userConf = ConfigFactory.parseFile(new File("scavenger.conf"))
    val generalConfig = 
      userConf.getConfig("scavenger") withFallback 
      userConf withFallback
      referenceConf
    val nodeSpecificConfig = extractNodeConfig(generalConfig)
    actorSystem = ActorSystem("scavenger", nodeSpecificConfig)
    initializeActors(actorSystem, nodeSpecificConfig)
  }

  def scavengerShutdown(): Unit = {
    actorSystem.shutdown()
  }
}