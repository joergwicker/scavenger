package scavenger.demo.clustering;

import java.util.List;

/**
 * 
 * T - The type given to all the TreeNode objects
 * Y - The type of the value to be compared be the ResultHandler 
 */
public abstract class ResultHandler<T, Y> implements java.io.Serializable
{   
    public abstract void addAttributeValue(Y value); 

    /**
     * Used by Diana clustering.
     * Clusters should be extracted and passed to handleResults(List<TreeNode<T>> nodes) 
     */
    public abstract void handleResults(TreeNode<T> node);
    
    /**
     * Used by bottom up clustering.
     * Clusters should be extracted and passed to handleResults(List<TreeNode<T>> nodes) 
     */
    public abstract void handleResults(TreeNodeList<T> node);
    
    /**
     * @param nodes List of the clusters to be evaluated 
     */
    public abstract void handleResults(List<TreeNode<T>> nodes);
    
}