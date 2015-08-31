package scavenger.app;

import scavenger.*;
import scavenger.app.ScavengerAppJ;

import java.util.function.Function;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.Future;

import akka.dispatch.Futures;
import akka.util.Timeout;
import static akka.dispatch.Futures.future;
import static akka.dispatch.Futures.sequence;


/**
 * Singleton version of ScavengerAppJ
 *
 * This way scavenger can be used without extending ScavengerAppJ,
 *  and the same ScavengerAppJ instance can be accessed by multiple instances of a class trying to run jobs.
 *
 * @author Helen Harman
 */
public class ScavengerSingleApp extends ScavengerAppJ
{
    ///// singleton code /////
    private static ScavengerSingleApp instance = null;
    
    protected ScavengerSingleApp() 
    {
        super(); 
    }
    public static ScavengerSingleApp getInstance() 
    {
        if(instance == null) 
        {
            
           /* WorkerMainJ workerMain = new WorkerMainJ();
            workerMain.scavengerInit();
            SeedMainJ seedMain = new SeedMainJ();
            seedMain.scavengerInit();*/
            instance = new ScavengerSingleApp();
            
        }
        return instance;
    } 
    //////////////////////////
    
    // TODO friendly automatic shutdown
    public void endScavengerApp() 
    {
        scavengerShutdown();
    }
    
    /**
     * Used to create a scavenger algorithm
     * @return 
     */
    public package$ getAlgorithm()
    {
        return scavengerAlgorithm;
    }
    
    /**
     * Used to create a scavenger computation
     * @return 
     */
    public Computation$ getComputation()
    {
        return scavengerComputation;
    }
    
    /**
     * scavengerContext() is used to submit jobs to scavenger
     * @return 
     */
    public Context getScavengerContext()
    {
        return scavengerContext();
    }
}

