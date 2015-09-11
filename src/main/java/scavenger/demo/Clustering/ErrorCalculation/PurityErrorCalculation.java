package scavenger.demo.clustering.errorCalculation;

import scavenger.demo.clustering.*;
import scavenger.demo.clustering.distance.*;
import scavenger.demo.clustering.resultHandler.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * 
 * @see ResultHandlerStringValues
 * @author Helen Harman
 */
public class PurityErrorCalculation<T> implements ErrorCalculation<T>, java.io.Serializable 
{
    private double errorThreshold;
    private double lastError;
    private ResultHandlerStringValues resultHandlerStringValues;
    
    public PurityErrorCalculation(double errorThreshold, ResultHandlerStringValues resultHandlerStringValues)
    {
        this.resultHandlerStringValues = resultHandlerStringValues;
        this.errorThreshold = errorThreshold;
    }
    
    /**
     *
     * @param clusters List of the leaf clusters
     * @param dianaDistanceFunctions Contains the diameter and average distance calculations
     */
    @Override
    public boolean isClustered(List<TreeNode<T>> clusters, DianaDistanceFunctions diana)
    {
        if (calculateError(clusters, diana) < errorThreshold)
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
        lastError = resultHandlerStringValues.handleResults(clusters);
        System.out.println("lastError = " + lastError);
        return lastError;
    }
    
    
    @Override
    public double getLastError()
    {
        return lastError;
    }
}