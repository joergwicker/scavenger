package scavenger.app;

import akka.actor.*;
import com.typesafe.config.Config;
import scavenger.backend.seed.Seed;
import scavenger.backend.seed.*;
import scavenger.backend.worker.*;
/** 
  * Allows a ScanvengerNode to be created in Java
  * See app/ScavengerNode.scala for more details
  * 
  * @author Helen Harman
  */
abstract class ScavengerNodeJ implements ScavengerNode {
    private ActorSystem actorSystem;
    Seed$ seed = Seed$.MODULE$;
    Worker$ worker = Worker$.MODULE$;
    
    /**
     * Overrides the = operator for ScavengerNode.actorSystem
     */
    public void scavenger$app$ScavengerNode$$actorSystem_$eq(ActorSystem actorSystem)
    {
        this.actorSystem = actorSystem;
    }
    
    public ActorSystem scavenger$app$ScavengerNode$$actorSystem()
    {
        return this.actorSystem;
    }

    
    public void scavengerShutdown()
    {
        ScavengerNode$class.scavengerShutdown(this);  
    }
  
    public void scavengerInit()
    {
        ScavengerNode$class.scavengerInit(this);
    }
  
  
    public ActorPath extractSeedPath(Config generalConfig)
    {
        return ScavengerNode$class.extractSeedPath(this, generalConfig);
    }
}
