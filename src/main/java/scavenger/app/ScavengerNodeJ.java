package scavenger.app;

import akka.actor.*;
import com.typesafe.config.Config;
import scavenger.backend.seed.Seed;
import scavenger.backend.seed.*;
import scavenger.backend.worker.*;
/** 
  * Allows a ScanvengerNode to be created in Java
  * Used for the general Worker, Seed and Master code.
  *
  * ScavengerNode is a scala trait. Therefore, when it is compiled ScavengerNode.class contains an interface; and ScavengerNode$class.class contains the implementation. 
  *  So, here we must "implements ScavengerNode" and implement its methods by calling ScavengerNode$class methods.
  * 
  *
  * @see app/ScavengerNode.scala
  * @author Helen Harman
  */
abstract class ScavengerNodeJ implements ScavengerNode {
    private ActorSystem actorSystem;
    Worker$ worker = Worker$.MODULE$;
    Seed$ seed = Seed$.MODULE$; 
    
    /**
     * Overrides the = operator for ScavengerNode.actorSystem
     *
     * @param actorSystem (see http://doc.akka.io/api/akka/2.0/akka/actor/ActorSystem.html)
     */
    public void scavenger$app$ScavengerNode$$actorSystem_$eq(ActorSystem actorSystem)
    {
        this.actorSystem = actorSystem;
    }
    
    /**
     * Getter for actorSystem
     * @return The ActorSystem
     */
    public ActorSystem scavenger$app$ScavengerNode$$actorSystem()
    {
        return this.actorSystem;
    }

    /**
     * Stops the actorSystem 
     * Will call ActorSystem.shutdown()
     */
    public void scavengerShutdown()
    {
        ScavengerNode$class.scavengerShutdown(this);  
    }
  
    /**
     * Starts the actor. If Actor is a Worker or Master, the will contect to the seed.
     */
    public void scavengerInit()
    {
        ScavengerNode$class.scavengerInit(this);
    }
  
    /**
     * Gets the seed path from the scavenger.conf file
     */
    public ActorPath extractSeedPath(Config generalConfig)
    {
        return ScavengerNode$class.extractSeedPath(this, generalConfig);
    }
}
