package scavenger.app;

import scavenger.*;
import scavenger.app.LocalScavengerApp;

/**
 * Used to create a Java scavenger application.
 * 
 * For an example see scavenger.demo.DemoJ
 */
public abstract class ScavengerAppJ extends DistributedScavengerApp 
{  
    protected package$ scavengerAlgorithm = package$.MODULE$; // Used to create a scavenger Algorithm
                                                              // For example : 
                                                              //   Algorithm<T, T> algorithm = scavengerAlgorithm.expensive("id", myScavengerFunction);
                                                              
    protected Computation$ scavengerComputation = Computation$.MODULE$; // Used to create a scavenger Computation
                                                                        // For example :
                                                                        //   Computation<T> comp = scavengerComputation.apply("Computation_1", myData)
                                                                        //   Computation<T> comp2 = algorithm.apply(compData)
    /**
     *
     */
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

