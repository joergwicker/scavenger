package scavenger.demo.clustering.errorCalculation;

import scavenger.demo.clustering.*;
import scavenger.demo.clustering.distance.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Interface for checking if final clusters are "good".
 *
 * @author Helen Harman
 */
public interface ErrorCalculation<T> 
{

    /**
     *
     * @param clusters List of the leaf clusters
     * @param dianaDistanceFunctions Contains the diameter and average distance calculations
     */
    public abstract boolean isClustered(List<TreeNode<T>> clusters, DianaDistanceFunctions dianaDistanceFunctions);

    /**
     * Returns the error from the previous call to isClustered()
     * Used to keep a record of the result with the smallest error
     */
    public abstract double getLastError();
}