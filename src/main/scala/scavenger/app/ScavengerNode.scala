package scavenger.app

import akka.actor.{ActorPath, ActorSystem}
import java.io.File
import com.typesafe.config._

/** Trait for programs with a `main`-function,
  * that loads Scavenger-configuration, creates actor system, starts actors.
  *
  * @since 2.1
  * @author Andrey Tyukin
  */
private[app] trait ScavengerNode {

  private var actorSystem: ActorSystem = _

  /** Enables each type of node to provide a slightly different
    * version of the configuration that is used to create the
    * `ActorSystem`.
    */
  private[app] def extractNodeConfig(generalConfig: Config): Config

  /** Depending on the type of node, this method has to
    * spawn a master/worker/seed actor in the `system`.
    *
    * The configuration is put together from the application configuration file
    * and `reference.conf` in `resources` folder of Scavenger.
    */
  private[app] def initializeActors(
    system: ActorSystem, 
    nodeSpecificConfig: Config
  ): Unit


  /** This method composes `ActorPath` of the seed node from various
    * entries in the specified configuration.
    *
    * It makes use of the following entries in the `seed`-subconfig:
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
  private[app] def extractSeedPath(generalConfig: Config): ActorPath = {
    val seedConfig = generalConfig.getConfig("seed")
    val seedIp = seedConfig.getString("akka.remote.netty.tcp.hostname")
    val seedPort = seedConfig.getString("akka.remote.netty.tcp.port")
    val seedPathStr = s"akka.tcp://scavenger@${seedIp}:${seedPort}/user/seed"
    ActorPath.fromString(seedPathStr)
  }

  /** Loads configuration files and initializes an
    * actor system, together with the right set of actors.
    *
    * TODO: this description is outdated
    *
    * The configuration files are loaded from the following paths:
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
    * 3) Following additional JVM-properties can be used to set the 
    *    IP and port of worker nodes: -DnodeIp=123.456.0.78 -DnodePort=8765
    *    
    * 4) All other parameters can be specified as JVM properties as well, but
    *    keep in mind that values in 'scavenger.conf' have higher precedence
    *    than anything else.
    *
    */
  def scavengerInit(): Unit = {
    val conf = ConfigFactory.load()
    // TODO: all this seemed to be a bad idea, just remove it completely
    // use system property -Dconfig.file=/path/to/scavenger.conf instead
    // of all this mess!
    // 
    // val scavengerConfPath_null = System.getProperty("scavengerConfPath")
    // val scavengerConfPath = 
    //   if (scavengerConfPath_null == null) "scavenger.conf"
    //   else scavengerConfPath_null
    // val scavengerConfFile = new File(scavengerConfPath)
    // if (!scavengerConfFile.exists) {
    //   throw new FileNotFoundException("Could not find the user-modified " +
    //     "configuration file. Expected a path to this file passed as a " +
    //     "property to the jvm: " +
    //     "-DscavengerConfPath=/path/to/my/scavenger.conf"
    //   )
    // }
    // val userConf = ConfigFactory.parseFile(scavengerConfFile)
    var generalConfig = ConfigFactory.empty()
    try{
      generalConfig = 
      conf.getConfig("scavenger") withFallback conf
    } catch { // for backwards compatibility
      case ex: ConfigException.Missing => {val userConf = ConfigFactory.parseFile(new File("scavenger.conf"))
        generalConfig = userConf.getConfig("scavenger") withFallback conf
       }
    }
      // referenceConf
    val nodeSpecificConfig = extractNodeConfig(generalConfig)
    actorSystem = ActorSystem("scavenger", nodeSpecificConfig)
    initializeActors(actorSystem, nodeSpecificConfig)
  }

  def scavengerShutdown(): Unit = {
    actorSystem.shutdown()
  }
}
