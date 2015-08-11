package scavenger.app;

import scavenger.*;
import scavenger.app.LocalScavengerApp;

public abstract class ScavengerAppJ extends DistributedScavengerApp 
{  
    protected package$ scavengerAlgorithm = package$.MODULE$;
    protected Computation$ scavengerComputation = Computation$.MODULE$;
    
    public ScavengerAppJ()
    {
        super();
        
        scavengerInit();
        try // must sleep after scavengerInit() is called.
        {
            Thread.sleep(3000);
        }
        catch (Exception e) 
        { 
            e.printStackTrace();
        } 
    }
}

