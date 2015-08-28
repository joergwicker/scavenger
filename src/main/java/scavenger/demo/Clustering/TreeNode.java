package scavenger.demo.clustering;
import scavenger.demo.clustering.distance.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Queue;

/**
 * A node within the cluster hierarchy.
 */
public class TreeNode<T> implements java.io.Serializable
{
    private List<DataItem<T>> data;
    private TreeNode parent;
    private TreeNode childLeft = null;
    private TreeNode childRight = null;
    
    private int splitNumber;    
    private List<Integer> toBeSplitOn = new ArrayList<Integer>(); // indexes of items

    public int getSplitNumber()
    {
        return splitNumber;
    }

    /**
     * Constructor for the root node.
     *
     * @param data The DataItems being clustered
     */
    public TreeNode(List<DataItem<T>> data)
    {
        this.data = data;
        splitNumber = 0;
        
    }
    
    /**
     * Constructor for all none root nodes.
     * 
     * @param data The DataItems being clustered
     * @param parent
     */
    public TreeNode(List<DataItem<T>> data, TreeNode parent)
    {
        this.data = data;
        this.parent = parent;
    }
    
    public void setToBeSplitOn(List<Integer> toBeSplitOn)
    {
        this.toBeSplitOn = toBeSplitOn;
    }
    
    public int countSplits(TreeNode<T> root)
    {
        int count = 0;
        if (root.getChildLeft() != null)
        {     
            count = count + 1;
            count = count + countSplits(root.getChildLeft());
            count = count + countSplits(root.getChildRight());
        }
        System.out.println("countSplits : " + count);
        return count;
    }
    
    public TreeNode<T> getRoot()
    {
        TreeNode<T> node = this;
        while (node.getParent() != null)
        {
            node = node.getParent();
        }
        return node;    
    }
    
    public void removeHigherSplitNodes(TreeNode<T> root)
    {
        System.out.println("removeHigherSplitNodes");
        if ((root.getChildLeft() != null) && (root.getChildLeft().getSplitNumber() > this.splitNumber))
        {
            System.out.println("Set null");
            root.setChildren(null, null);
        }
        else if (root.getChildLeft() != null)
        {
            System.out.println("call removeHigherSplitNodes");
            removeHigherSplitNodes(root.getChildLeft());
            removeHigherSplitNodes(root.getChildRight());
        }
    }
    
    /**
     * @return 
     */ 
    public List<DataItem<T>> getData()
    {
        return data;
    }
    
    public void calculateSplits()
    {
        splitNumber = countSplits(getRoot());
    }
    
    /**
     *
     * @param childLeft 
     * @param childRight
     */
    public void setChildren(TreeNode childLeft, TreeNode childRight)
    {
        this.childLeft = childLeft;
        this.childRight = childRight;
    }
    
    //// Getters //// 
    
    public TreeNode getChildLeft()
    {
        return childLeft;
    }
    
    public TreeNode getChildRight()
    {
        return childRight;
    }
    
    public TreeNode getParent()
    {
        return parent;
    }
    
    
    public List<Integer> getToBeSplitOn()
    {
        return toBeSplitOn;
    }
    
    /////////////////
    
    public void print() 
    {
        for (DataItem value : data)
        {
            System.out.print(", " + value.getId());
        }
        System.out.println(" : " + splitNumber);
    }
    
}