package scavenger.demo.clustering.distance;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

import scavenger.app.ScavengerAppJ;


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
 *  Holds the average and diameter calculations for Diana.
 *
 */
abstract class DianaDistanceFunctions<T>  extends ScavengerAppJ implements java.io.Serializable
{
    protected DistanceMeasureSelection[] dataInfo;
    
    protected DianaDistanceFunctions()
    {
        super();
    }
    
    /**
     * Calculates the diameters of all the given clusters
     * Uses scavenger. Means that if the diameter of a cluster has already been calculated it is not re-calculated.
     * 
     * @param clusters The clusters who's diameters are to be calculated
     * @return the diameters of the clusters
     */
    protected List<Double> calculateClusterDiameters(List<TreeNode<T>> clusters)
    {
        System.out.println("calculateClusterDiameter : make future" );
        List<Double> distances = new ArrayList<Double>(clusters.size());
        
        List<Future<Double>> futures = new ArrayList<Future<Double>>();
        for(int j = 0; j < clusters.size(); j++)
        {
            Double distance = 0.0;
            // create the scavenger algorithm
            ScavengerFunction<Double> run = new ClusterDiameter(clusters.get(j).getData());
            Algorithm<Double, Double> algorithm = scavengerAlgorithm.expensive("ClusterDiameter", run).cacheGlobally();
            
            // create the scavenger computation (data) that will be passed to the algorithm
            Computation<Double> computationData = scavengerComputation.apply("distance"+clusters.get(j).getData(), distance).cacheGlobally();        
            
            // tell scavenger that we want to apply to algorithm to the computation (data)
            Computation<Double> computation1 = algorithm.apply(computationData);
            
            // submit the job to scavenger
            Future<Double> future = scavengerContext().submit(computation1);
            futures.add(future);
        }
        
        // wait for all the diameters to be calculated. 
        Future<Iterable<Double>> allTogether = Futures.sequence(futures, scavengerContext().executionContext());
        try
        {
            distances = (List<Double>)Await.result(allTogether, (new Timeout(Duration.create(40, "seconds")).duration()));
        }
        catch(Exception e) 
        { 
            e.printStackTrace(); 
        }
        
        return distances;    
    }
    
    /**
     * Calculates the diameter of a cluster. 
     * The diameter of a cluster is the largest average distance between a DataItem and the other DataItems in the cluster. 
     *
     * @see calculateClusterDiameters(List<TreeNode<T>> clusters)
     */
    class ClusterDiameter extends ScavengerFunction<Double> 
    {
        private List<DataItem<T>> cluster;
        public ClusterDiameter(List<DataItem<T>> cluster)
        {
            this.cluster = cluster;
        }
        
        public Double call()
        { 
            System.out.println("ClusterDiameter : run future" );
            double maxDistance = 0.0;
            
            for (int i = 0; i < cluster.size(); i++)
            {
                double distance = calculateAverage(cluster, i);
                
                if (distance > maxDistance)
                {
                    maxDistance = distance;
                }
            }    
            return maxDistance;
        }        
    }
    
    /**
     * Max distance between two elements in a cluster
     * Uses scavenger. Means that if the diameter of a cluster has already been calculated it is not re-calculated.
     * 
     * @param cluster The cluster who's diameter is to be calculated
     * @return the diameter of the cluster
     */
    /*protected double calculateClusterDiameter(List<DataItem<T>> cluster)
    {
        System.out.println("calculateClusterDiameter : make future" );
        double distance = 0.0;
        ScavengerFunction<Double> run = new ClusterDiameter(cluster);
        Computation<Double> computationData = scavengerComputation.apply("distance"+cluster, distance).cacheGlobally();
        Algorithm<Double, Double> algorithm = scavengerAlgorithm.expensive("ClusterDiameter", run).cacheGlobally();
        Computation<Double> computation1 = algorithm.apply(computationData);
        Future<Double> future = scavengerContext().submit(computation1);
        
        try
        {
            distance = (Double)Await.result(future, (new Timeout(Duration.create(40, "seconds")).duration()));
        }
        catch(Exception e) 
        { 
            e.printStackTrace(); 
        }
        return distance;
        
        /
        double maxDistance = 0.0;
        
        for (int i = 0; i < cluster.size(); i++)
        {
            double distance = calculateAverage(cluster, i);

            if (distance > maxDistance)
            {
                maxDistance = distance;
            }
        }    
        return maxDistance;/
    }*/
    
    
    

    
    /**
     * 
     * The node returned will start the new cluster
     *
     * @param cluster 
     * @return The index of the item with the highest average distance
     */
    protected int getIndexWithHighestAverageIndex(List<DataItem<T>> cluster)
    {
        int indexOfHighestAverage = 0;
        double highestAverege = 0;
        for (int i = 0; i < cluster.size(); i++)
        {
            double average = calculateAverage(cluster, i); 
            if (average > highestAverege)
            {
                indexOfHighestAverage = i;
                highestAverege = average;
            }
        }
        return indexOfHighestAverage;
    }
    
    /**
     * Runs calculateAverageSimple if only one distance measure is being used; else runs calculateAverageComplex
     * 
     * @param cluster
     * @param index The index of the item, who's average distance is being calculated
     * @return The average distance
     */ 
    protected double calculateAverage(List<DataItem<T>> cluster, int index)
    {
        if (dataInfo.length == 1 )
        {
            return calculateAverageSimple(cluster, index); 
        }
        else
        {
            return calculateAverageComplex(cluster, index);
        }            
    }
    
    /**
     * Calculates the average distance, when one distance measure is being used.
     *
     * @param cluster
     * @param index The index of the item, who's average distance is being calculated
     * @return The average distance
     */
    private double calculateAverageSimple(List<DataItem<T>> cluster, int index)
    {
        double total = 0;
        for(int i = 0; i < cluster.size(); i++)
        {
            if (index == i) 
            {
                continue;
            }
            //dataInfo[0].getDistanceMeasure().setScavengerContext(scavengerContext());//TODO rm, use singleton
            total = total + dataInfo[0].getDistanceMeasure().getDistance(cluster.get(index).getData(), cluster.get(i).getData());            
        }
        return total / cluster.size();
    }
    
    /**
     * Calculates the average distance, when multiple distance measure is being used.
     *
     * @param cluster
     * @param index The index of the item, who's average distance is being calculated
     * @return The average distance
     */
    private double calculateAverageComplex(List<DataItem<T>> cluster, int index)
    {
        double total = 0;
        for(int i = 0; i < cluster.size(); i++)
        {
            double subTotal = 0;
            int numberOfItems = 0;
            if (index == i) 
            {
                continue;
            }
            for(DistanceMeasureSelection distanceMeasure : dataInfo)
            {
                for(String id : distanceMeasure.getIds())
                {
                    try
                    {
                        //distanceMeasure.getDistanceMeasure().setScavengerContext(scavengerContext());//TODO rm, use singleton
                        subTotal = subTotal + (distanceMeasure.getDistanceMeasure().getDistance(cluster.get(index).getHashMap().get(id), cluster.get(i).getHashMap().get(id)) * distanceMeasure.getWeight());
                        numberOfItems = numberOfItems + 1;
                    }
                    catch(Exception ex) 
                    {
                        ex.printStackTrace();
                    }
                }
            }
            total = total + (subTotal / numberOfItems);
        }
        return total / cluster.size();
    }
}


