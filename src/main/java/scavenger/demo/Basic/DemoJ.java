package scavenger.demo;

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
 * A basic example of how to create a Scavenger Java application.
 */
class DemoJ extends ScavengerAppJ
{
    
    static final ScavengerFunction<Integer> f0 = new ScavengerFunction<Integer>()
    {
        // Value computation is being applied to can be accessed using "this.value"
        // To submit a new job this.ctx.submit(...) can be used
        public Integer call() 
        {
            try{
                Thread.yield();
                Thread.sleep(3000);
            }catch (Exception e) { 
                e.printStackTrace();
            }  
            System.out.println(this.toString() + " was paused");
            return this.value*this.value;
        }
    };
    
    static final ScavengerFunction<Integer> f1 = new ScavengerFunction<Integer>()
    {
        // Value computation is being applied to can be accessed using "this.value"
        // To submit a new job this.ctx.submit(...) can be used
        public Integer call() 
        {
            try{
                Thread.yield();
                Thread.sleep(3000);
            }catch (Exception e) { 
                e.printStackTrace();
            }  
            System.out.println(this.toString() + " was paused");
            return this.value*2;
        }
    };


    public DemoJ()
    {
        super();        
    }    
    
    /**
     * Applys f0 to the data (Integer 2) twice
     */
    public void runDemo()
    {
        startScavenger();
        Computation<Integer> computationData = scavengerComputation.apply("Computation_1", 2).cacheGlobally();
        Algorithm<Integer, Integer> algorithm = scavengerAlgorithm.expensive("id", f0).cacheGlobally();
        Algorithm<Integer, Integer> algorithm2 = scavengerAlgorithm.expensive("id", f1).cacheGlobally();
        
        Computation<Integer> computation1 = algorithm.apply(computationData);
        Computation<Integer> computation2 = algorithm.apply(computation1);
        Computation<Integer> computation3 = algorithm2.apply(computation2);
        
        // Submit the computation to scavenger
        Future<Integer> futureS = scavengerContext().submit(computation1);
        Future<Integer> futureS2 = scavengerContext().submit(computation2);
        Future<Integer> futureS3 = scavengerContext().submit(computation3);
        
        // Combind all the futures into one
        List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
        futures.add(futureS);
        futures.add(futureS2);
        futures.add(futureS3);
        
        Future<Iterable<Integer>> allTogether = Futures.sequence((Iterable<Future<Integer>>)futures, scavengerContext().executionContext());
        
        System.out.println(scavengerContext().dumpCacheKeys());
        
        // Wait for the futures to finish
        //allTogether.onSuccess(new PrintResults<Iterable<Integer>>(), scavengerContext().executionContext());
        try
        {
            Iterable<Integer> results = (Iterable<Integer>)Await.result(allTogether, (new Timeout(Duration.create(40, "seconds")).duration()));
            for (Integer i : results)
            {
                System.out.println("Results : " + i);
            }
        }
        catch(Exception e) 
        { 
            e.printStackTrace(); 
        }
        
        scavengerShutdown(); 
    }
    
    
    public static void main(final String[] args)
    {    
        DemoJ demo = new DemoJ();
        demo.runDemo();
    }
}

