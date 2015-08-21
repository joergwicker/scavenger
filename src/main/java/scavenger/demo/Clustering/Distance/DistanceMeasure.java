package scavenger.demo.clustering.distance;

import scavenger.*;

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
 * All distance measures should implement DistanceMeasure
 */
abstract class DistanceMeasure<T> implements java.io.Serializable// extends ScavengerFunction<Double>
{
    public double getDistance(T value1, T value2)
    {
        return calculateDistance(value1, value2);
    }
    public abstract double calculateDistance(T value1, T value2);
    
    
    
    // experimented with using scavenger to calculate the distance. TODO rm and change this back to interface.
   /* private T value1;
    private T value2;
    private package$ scavengerAlgorithm = package$.MODULE$;
    private Computation$ scavengerComputation = Computation$.MODULE$;
    private Context scavengerContext = null;
    
    public void setScavengerContext(Context scavengerContext)
    {
        this.scavengerContext = scavengerContext;
    }
    
    public double getDistance(T value1, T value2)
    {
        double distance = 0.0;
        this.value1 = value1;
        this.value2 = value2;
        //ScavengerFunction<Double> run = new CreateNewSpliter<T>();
        Computation<Double> computationData = scavengerComputation.apply("Values_"+value1 + "_" + value2, distance).cacheGlobally();
        Algorithm<Double, Double> algorithm = scavengerAlgorithm.expensive("calculateDistance", this).cacheGlobally();
        Computation<Double> computation1 = algorithm.apply(computationData);
        Future<Double> future = scavengerContext.submit(computation1);
        
        try
        {
            distance = (Double)Await.result(future, (new Timeout(Duration.create(40, "seconds")).duration()));
        }
        catch(Exception e) 
        { 
            e.printStackTrace(); 
        }
        return distance;
    }
    
    public Double call()
    {
        return calculateDistance(value1, value2);
    }*/
    
}