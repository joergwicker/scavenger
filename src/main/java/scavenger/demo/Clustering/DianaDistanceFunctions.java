package scavenger.demo.clustering.distance;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;


abstract class DianaDistanceFunctions<T>
{
    protected DataInformation[] dataInfo;
    
    /**
     * Max distance between two elements in a cluster
     */
    protected double calculateClusterDiameter(List<DataItem<T>> data)
    {
        double maxDistance = 0.0;
        
        for (int i = 0; i < data.size(); i++)
        {
            double distance = calculateAverage(data, i);

            if (distance > maxDistance)
            {
                maxDistance = distance;
            }
        }    
        return maxDistance;
    }

    
    /**
     * The node returned will start the new cluster
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
     * Runs the correct calculateAverage function
     */ 
    protected double calculateAverage(List<DataItem<T>> data, int index)
    {
        if (dataInfo.length == 1 )
        {
            return calculateAverageSimple(data, index); 
        }
        else
        {
            return calculateAverageComplex(data, index);
        }            
    }
    
    /**
     * Single distance measure being used
     */
    private double calculateAverageSimple(List<DataItem<T>> data, int index)
    {
        double total = 0;
        for(int i = 0; i < data.size(); i++)
        {
            if (index == i) 
            {
                continue;
            }
            total = total + dataInfo[0].getDistanceMeasure().getDistance(data.get(index).getData(), data.get(i).getData());            
        }
        return total / data.size();
    }
    
    /**
     * Multiple distance measures being used
     */
    private double calculateAverageComplex(List<DataItem<T>> data, int index)
    {
        double total = 0;
        for(int i = 0; i < data.size(); i++)
        {
            double subTotal = 0;
            int numberOfItems = 0;
            if (index == i) 
            {
                continue;
            }
            for(DataInformation info : dataInfo)
            {
                for(String id : info.getIds())
                {
                    try
                    {
                        subTotal = subTotal + (info.getDistanceMeasure().getDistance(data.get(index).getHashMap().get(id), data.get(i).getHashMap().get(id)) * info.getWeight());
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
        return total / data.size();
    }
}


