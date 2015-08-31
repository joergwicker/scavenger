package scavenger.app;

import scavenger.*;
import scavenger.app.LocalScavengerApp;

/**
 * Used to run a Java scavenger application locally (without having to start a Seed and Workers).
 * 
 * For an example see scavenger.demo.LocalDemoJ
 */
public abstract class LocalScavengerAppJ extends LocalScavengerApp 
{  
    protected package$ scavengerAlgorithm = package$.MODULE$; // @see ScavengerAppJ                                                              
    protected Computation$ scavengerComputation = Computation$.MODULE$; 
    
    /**
     * 
     * @param numWorkers
     */
    public LocalScavengerAppJ(int numWorkers)
    {
        super(numWorkers);   
    } 
    
    protected boolean scavengerStarted = false;
    public void startScavenger()
    {
        if (!scavengerStarted)
        {
            scavengerInit();
            try // must sleep after scavengerInit() is called.
            {
                Thread.sleep(3000);
            }
            catch (Exception e) 
            { 
                e.printStackTrace();
            } 
            scavengerStarted = true;
        }
    }
}

