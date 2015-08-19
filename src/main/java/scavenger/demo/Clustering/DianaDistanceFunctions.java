package scavenger.demo.clustering.distance;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

import scavenger.app.LocalScavengerAppJ;//ScavengerAppJ


/**
 *  Holds the average and diameter calculations for Diana.
 *
 */
abstract class DianaDistanceFunctions<T>  extends LocalScavengerAppJ
{
    protected DistanceMeasureSelection[] dataInfo;
    
    protected DianaDistanceFunctions()
    {
        super(3);
    }
    
    /**
     * Max distance between two elements in a cluster
     * 
     * @param cluster The cluster who's diameter is to be calculated
     * @return the diameter of the cluster
     */
    protected double calculateClusterDiameter(List<DataItem<T>> cluster)
    {
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
     * Runs calculateAverageSimple if only on distance measure is being used; else runs calculateAverageComplex
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


