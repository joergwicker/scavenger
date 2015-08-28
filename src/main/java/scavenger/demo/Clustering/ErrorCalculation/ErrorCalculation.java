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
public interface ErrorCalculation<T> 
{

    public abstract boolean isClustered(List<TreeNode<T>> clusters, DianaDistanceFunctions diana);

    public abstract double getLastError();
}