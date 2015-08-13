package scavenger.app;

import scavenger.*;
import scavenger.app.LocalScavengerApp;


public abstract class LocalScavengerAppJ extends LocalScavengerApp 
{  
    protected package$ scavengerAlgorithm = package$.MODULE$;
    protected Computation$ scavengerComputation = Computation$.MODULE$;
    public LocalScavengerAppJ(int numWorkers)
    {
        super(numWorkers);   
        scavengerInit();
        try
        { // must sleep after calling scavengerInit()
            Thread.sleep(10000);
        }
        catch (Exception e) 
        { 
                e.printStackTrace();
        }      
    } 

}

