package scavenger.demo;


//import scala.language.postfixOps;
import scavenger.*;
import scavenger.app.LocalScavengerApp;
import java.util.function.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import akka.dispatch.ExecutionContexts;
import akka.dispatch.Mapper;

import scala.Function2;
import scala.Tuple2;
import scala.collection.immutable.WrappedString;
import scala.runtime.AbstractFunction2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.*;
import akka.util.Timeout;

import scala.concurrent.Future;
import static akka.dispatch.Futures.future;
import static akka.dispatch.Futures.sequence;
//import scavenger.backend.*;
//import scala.concurrent.ExecutionContext.Implicits.global;
import java.util.concurrent.Callable;
class LocalDemoJ extends LocalScavengerApp 
{
    static final Function2<Integer, Context, Future<Integer>> f0 = new AbstractFunction2<Integer, Context, scala.concurrent.Future<Integer>>() 
    {
        public Future<Integer> apply(Integer x, Context ctx) 
        {
            System.out.println("running");
            
            Callable f = new Power(x); 
            
            return future(f, ctx.executionContext());
        }
    };


    static class Power implements Callable<Integer>
    {
        private Integer x;
        public Power(Integer x)
        {
            this.x = x;
        }
        
        public Integer call() 
        {
            try{
                Thread.yield();
                Thread.sleep(3000);
            }catch (Exception e) { 
                e.printStackTrace();
            }  
            System.out.println(this.toString() + " was paused");
            return x*x;
        }
    }
    



    public LocalDemoJ(int numWorkers)
    {
        super(numWorkers);        
    }    
    
    public void runDemo()
    {
        scavengerInit();
        try{
            Thread.sleep(3000);
        }catch (Exception e) { 
                e.printStackTrace();
            } 
        package$ packageVar = package$.MODULE$;
        Computation$ computation = Computation$.MODULE$;

        
        Computation<Integer> computationData = computation.apply("Computation_1", 2).cacheGlobally();
        Algorithm<Integer, Integer> algorithm = packageVar.expensive("id", f0);
        Computation<Integer> computation1 = algorithm.apply(computationData);
        
        Future futureS = scavengerContext().submit(computation1);
        Future futureS2 = scavengerContext().submit(algorithm.apply(computation1));
        
        
        List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
        futures.add(futureS);
        futures.add(futureS2);
        
        Future<Iterable<Integer>> allTogether = Futures.sequence((Iterable<Future<Integer>>)futures, scavengerContext().executionContext());
        
        allTogether.onSuccess(new PrintResults<Iterable<Integer>>(), scavengerContext().executionContext());
        System.out.println(scavengerContext().dumpCacheKeys());
        
        try
        {
            Await.result(allTogether, (new Timeout(Duration.create(20, "seconds")).duration()));
        }
        catch(Exception e) { e.printStackTrace(); }
        scavengerShutdown(); 
        
        /*Timeout timeout = new Timeout(Duration.create(10, "seconds"));
        Integer result = 0;
        Integer result2 = 0;
        try{
            result = (Integer) Await.result(futureS, timeout.duration());
            result2 = (Integer) Await.result(futureS2, timeout.duration());
        } catch (Exception e) { 
                e.printStackTrace();
        } 
        System.out.println("hello");
        System.out.println(result);
        System.out.println(result2);*/
    }
    
    
    public static void main(final String[] args)
    {    
        LocalDemoJ localDemo = new LocalDemoJ(1);
        localDemo.runDemo();
    }
}

