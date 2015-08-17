package scavenger.app;

import akka.actor.*;
import com.typesafe.config.Config;
import scavenger.backend.seed.Seed;
import scavenger.backend.seed.*;
import scavenger.util.*;
/** 
  * Starts a worker node. 
  * See app/WorkerMain.scala for more details
  *
  * @author Helen Harman
  */
class WorkerMainJ extends ScavengerNodeJ 
{    
    /**
     * Gets the worker's configuration from the config file
     */
    public Config extractNodeConfig(Config generalConfig)
    {
        return generalConfig.getConfig("worker").withFallback(generalConfig);
    }
    
    /**
     * Gets the seed's configuration from the config file
     * Creates a new Worker actor within the ActorSystem. 
     */
    public void initializeActors(ActorSystem system, Config generalConfig)
    {                
        ActorPath seedPath = extractSeedPath(generalConfig);
        system.actorOf(worker.props(seedPath), scavenger.util.RandomNameGenerator.randomName());
    }

    public static void main(final String[] args)
    {
        WorkerMainJ workerMain = new WorkerMainJ();
        workerMain.scavengerInit();
    }
}
