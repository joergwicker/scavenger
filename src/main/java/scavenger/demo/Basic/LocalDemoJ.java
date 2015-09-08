package scavenger.demo;

import scavenger.*;
import scavenger.app.LocalScavengerAppJ;

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
 * A basic example of how to create a Local Scavenger Java application.
 */
class LocalDemoJ extends LocalScavengerAppJ {
    ScavengerFunction<Integer> f0 = new ScavengerFunction<Integer>()
    {
        // Value computation is being applied to can be accessed using "this.value"
        // To submit a new job this.ctx.submit(...) can be used
        public Integer call() 
        {
            try
            {
                Thread.sleep(3000);
            }
            catch (Exception e) 
            { 
                e.printStackTrace();
            }  
            System.out.println(this.toString() + " was paused");
            return this.value*this.value;
        }
    };

    public LocalDemoJ(int numWorkers)
    {
        super(numWorkers);        
    }    
    
    public void runDemo()
    {
        startScavenger();
        Computation<Integer> computationData = scavengerComputation.apply("Computation_1", 2).cacheGlobally();
        Algorithm<Integer, Integer> algorithm = scavengerAlgorithm.expensive("id", f0).cacheGlobally();
        
        Computation<Integer> computation1 = algorithm.apply(computationData);
        Computation<Integer> computation2 = algorithm.apply(computation1);
        
        // Submit the computations to scavenger
        Future<Integer> futureS = scavengerContext().submit(computation1);
        Future<Integer> futureS2 = scavengerContext().submit(computation2);
        
        // Combind all the futures into one
        List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
        futures.add(futureS);
        futures.add(futureS2);
        
        Future<Iterable<Integer>> allTogether = Futures.sequence(futures, scavengerContext().executionContext());
        
        System.out.println(scavengerContext().dumpCacheKeys());
        
        // for none blocking use allTogether.onSuccess
        //allTogether.onSuccess(new PrintResults<Iterable<Integer>>(), scavengerContext().executionContext());
        
        // Waits for the Futures to finish and prints the results
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
        LocalDemoJ localDemo = new LocalDemoJ(1);
        localDemo.runDemo();
    }
}

