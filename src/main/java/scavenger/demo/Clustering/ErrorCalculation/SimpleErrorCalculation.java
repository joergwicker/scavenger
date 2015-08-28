package scavenger.demo.clustering.errorCalculation;

import scavenger.demo.clustering.*;
import scavenger.demo.clustering.distance.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Used to check if the highest cluster diameter is above the given (maxDiameterThreshold) threshold.
 *
 * @author Helen Harman
 */
public class SimpleErrorCalculation<T> implements ErrorCalculation<T>, java.io.Serializable 
{
    private double maxDiameterThreshold;
    private double maxClusterDiameter;
    
    public SimpleErrorCalculation(double maxDiameterThreshold)
    {
        this.maxDiameterThreshold = maxDiameterThreshold;
    }
    
    /**
     *
     * @param clusters List of the leaf clusters
     * @param dianaDistanceFunctions Contains the diameter and average distance calculations
     */
    @Override
    public boolean isClustered(List<TreeNode<T>> clusters, DianaDistanceFunctions diana)
    {
        if (calculateError(clusters, diana) < maxDiameterThreshold)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     *
     * @param clusters List of the leaf clusters
     * @param dianaDistanceFunctions Contains the diameter and average distance calculations
     */
    public double calculateError(List<TreeNode<T>> clusters, DianaDistanceFunctions diana)
    {
        maxClusterDiameter = 0;
        for (TreeNode<T> cluster : clusters)
        {
            double clusterDiameter = diana.calculateClusterDiameter(cluster.getData());
            if (clusterDiameter > maxClusterDiameter)
            {
                maxClusterDiameter = clusterDiameter;
            }
        }
        return maxClusterDiameter;
    }
    
    @Override
    public double getLastError()
    {
        return maxClusterDiameter;
    }
}