package scavenger.app;

import akka.actor.*;
import com.typesafe.config.Config;
import scavenger.backend.seed.Seed;
import scavenger.backend.seed.*;
/** 
  * Starts a seed node. 
  *
  * @see app/SeedMain.scala 
  *
  * @author Helen Harman
  */
class SeedMainJ extends ScavengerNodeJ 
{    
    /**
     * Gets the seed's configuration from the config file
     */
    public Config extractNodeConfig(Config generalConfig)
    {
        return generalConfig.getConfig("seed").withFallback(generalConfig);
    }
    
    /**
     * Creates a new Seed actor within the ActorSystem.
     */
    public void initializeActors(ActorSystem system, Config generalConfig)
    {        
        system.actorOf(seed.props(), "seed");
    }

    public static void main(final String[] args)
    {
        SeedMainJ seedMain = new SeedMainJ();
        seedMain.scavengerInit(); 
    }
}
