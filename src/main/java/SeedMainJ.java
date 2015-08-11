package scavenger.app;

import akka.actor.*;
import com.typesafe.config.Config;
import scavenger.backend.seed.Seed;
import scavenger.backend.seed.*;
/** 
  * Starts a seed node. 
  * See app/SeedMain.scala for more details
  *
  * @author Helen Harman
  */
class SeedMainJ extends ScavengerNodeJ 
{    
    public Config extractNodeConfig(Config generalConfig)
    {
        return generalConfig.getConfig("seed").withFallback(generalConfig);
    }
    
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
