package scavenger.demo.clustering;
import scavenger.demo.clustering.distance.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Comparator;

/**
 * Used for Bottom-up clustering
 * Contains a list of all the leaf clusters
 */
public class TreeNodeList<T> implements java.io.Serializable, Comparator<TreeNodeList<T>>
{
    private List<Integer[]> joinNodes = new ArrayList<Integer[]>();
    private List<TreeNode<T>> treeNodeData = new ArrayList<TreeNode<T>>();

    private double error = 1;
    
    public TreeNodeList(){} // for the Comparator
    
    /**
     *
     */
    public TreeNodeList(List<TreeNode<T>> treeNodeData)
    {
        this.treeNodeData = treeNodeData;
    }
    
    /**
     *
     */
    public TreeNodeList(List<TreeNode<T>> treeNodeData, List<Integer[]> joinNodes)
    {
        this.treeNodeData = treeNodeData;
        this.joinNodes = joinNodes;
    }

    /////// Getters and Setters /////////
    public List<TreeNode<T>> getTreeNodeData()
    {
        return treeNodeData;
    }
    
    public List<Integer[]> getJoinNodes()
    {
        return joinNodes;
    }
    
    public void setJoinNodes(List<Integer[]> joinNodes)
    {
        this.joinNodes = joinNodes;
    }
    
    public void setError(double error)
    {
        this.error = error;
    }
    public double getError()
    {
        return error;
    }
    //////////////////////////////
    
    /**
     * Used by the priority queue
     */ 
    public int compare(TreeNodeList<T> node1, TreeNodeList<T> node2)
    {
        if (node1.getError() < node2.getError()) return -1;
        if (node1.getError() > node2.getError()) return 1;
        return 0;
    } 
    
    /**
     * For saving and displaying the results
     */
    public String print()
    {
        String str = "";
        for(TreeNode<T> node : treeNodeData)
        {
            str = str + node.getRoot().printTree();
        }
        return str;
    }
 }