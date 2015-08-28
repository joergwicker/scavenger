package scavenger.demo.clustering.distance;
import scavenger.demo.clustering.*;
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
public abstract class DistanceMeasure<T> implements java.io.Serializable//extends ScavengerFunction<Double>//implements java.io.Serializable//
{
    public double getDistance(T value1, T value2)
    {
        return calculateDistance(value1, value2);
    }
    
    
    public abstract double calculateDistance(T value1, T value2);
    
    // experimented with using scavenger to calculate the distance. TODO rm and change this back to interface.
   /* private T value1;
    private T value2;
    
    
    public void setValues(T value1, T value2)
    {
        this.value1 = value1;
        this.value2 = value2;
    }
    
    public Double call()
    {
        System.out.println("calculateDistance called");
        return calculateDistance(value1, value2);
    }*/
    
}