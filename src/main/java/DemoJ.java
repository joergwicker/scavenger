package scavenger.demo;

import scavenger.*;
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
import java.util.concurrent.Callable;
import scavenger.app.DistributedScavengerApp;

class DemoJ extends DistributedScavengerApp
{
    static final Function2<Integer, Context, Future<Integer>> f0 = new Function<Integer, Context, scala.concurrent.Future<Integer>>() 
    {
        public Future<Integer> apply(Integer x, Context ctx) 
        {
            System.out.println("running");
            
            Callable<Integer> f = new Power(x); 
            
            return future(f, ctx.executionContext());
        }
    };


    static abstract class Function<X, Y, Z> extends AbstractFunction2<X, Y, Z> implements java.io.Serializable{}

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
    



    public DemoJ()
    {
        super();        
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
        Algorithm<Integer, Integer> algorithm = packageVar.expensive("id", f0).cacheGlobally();
        Computation<Integer> computation1 = algorithm.apply(computationData);
        
        Future<Integer> futureS = scavengerContext().submit(computation1);
        Future<Integer> futureS2 = scavengerContext().submit(algorithm.apply(computation1));
        
        
        List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
        futures.add(futureS);
        futures.add(futureS2);
        
        Future<Iterable<Integer>> allTogether = Futures.sequence((Iterable<Future<Integer>>)futures, scavengerContext().executionContext());
        
        allTogether.onSuccess(new PrintResults<Iterable<Integer>>(), scavengerContext().executionContext());
        System.out.println(scavengerContext().dumpCacheKeys());
        
        try
        {
            Await.result(allTogether, (new Timeout(Duration.create(40, "seconds")).duration()));
        }
        catch(Exception e) { e.printStackTrace(); }
        scavengerShutdown(); 
    }
    
    
    public static void main(final String[] args)
    {    
        DemoJ demo = new DemoJ();
        demo.runDemo();
    }
}

