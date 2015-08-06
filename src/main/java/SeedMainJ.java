package scavenger.app;

import akka.actor.*;
import com.typesafe.config.Config;
import scavenger.backend.seed.Seed;
import scavenger.backend.seed.*;
/** 
  *
  * 
  * @author 
  */
class SeedMainJ extends ScavengerNodeJ 
{
    
    public Config extractNodeConfig(Config generalConfig)
    {
        return generalConfig.getConfig("seed");// withFallback generalConfig
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
