package scavenger.demo.clustering.distance;
import scavenger.demo.clustering.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * 
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