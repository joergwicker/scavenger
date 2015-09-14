package scavenger.demo.clustering;
import scavenger.demo.clustering.distance.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Comparator;

/**
 * A node within the cluster hierarchy.
 */
public class TreeNode<T> implements java.io.Serializable, Comparator<TreeNode<T>>
{
    private List<DataItem<T>> data;
    private TreeNode parent = null;
    private TreeNode childLeft = null;
    private TreeNode childRight = null;
    
    
    // Used by Diana (not by bottom up clustering)
    private int splitNumber;    
    private List<Integer> toBeSplitOn = new ArrayList<Integer>(); // indexes of items
    private int splitHappenedOnIndex = 0;
    private double error = 1;
    
    public TreeNode(){} // for the Comparator

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
    
    public TreeNode(List<DataItem<T>> data, int splitNumber)
    {
        this.data = data;
        this.splitNumber = splitNumber;        
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
    
    public void setUpSplinterInfo(List<Integer> toBeSplitOn)
    {
        this.toBeSplitOn = toBeSplitOn;
    }
    
    public int countSplits()
    {
        splitNumber = getRoot().findLeafNodes().size();
        return splitNumber;
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
    
    public void setParent(TreeNode parent)
    {
        this.parent = parent;
    }
    
    public void setToBeSplitOn(List<Integer> toBeSplitOn)
    {
        this.toBeSplitOn = toBeSplitOn;
    }
    
    public void setError(double error)
    {
        this.error = error;
    }
    public double getError()
    {
        return error;
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
    
    /**
     * Finds and returns the root node
     */
    public TreeNode<T> getRoot()
    {
        TreeNode<T> node = this;
        while (node.getParent() != null)
        {
            node = node.getParent();
        }
        return node;    
    }    
    
    /**
     * @return 
     */ 
    public List<DataItem<T>> getData()
    {
        return data;
    }
    
    public int getSplitNumber()
    {
        return splitNumber;
    }
    
    /////////////////
    
    
    
    /**
     * Finds this trees leaf nodes
     */
    public List<TreeNode<T>> findLeafNodes()
    {
        return findLeafNodes(this);
    }    
    
    /**
     *
     * @param tree Initially should be the root node
     *
     * @return The leaf nodes
     */
    private List<TreeNode<T>> findLeafNodes(TreeNode<T> tree) 
    {
        List<TreeNode<T>> leaves = new ArrayList<TreeNode<T>>();
        
        if (tree.getChildLeft() == null)
        {
            leaves.add(tree);
        }
        else
        {
            leaves.addAll(findLeafNodes(tree.getChildLeft()) );
            leaves.addAll(findLeafNodes(tree.getChildRight()) );
        }
        return leaves;
    }
    
    
    /**
     * Finds the all the leaf nodes that were created before splinterNumber
     *
     * @param splinterNumber
     */
    public List<TreeNode<T>> findLeafNodes(int splinterNumber)
    {
        return findLeafNodes(this, splinterNumber);
    }
    
    /**
     * Finds the all the leaf nodes that were created before splinterNumber
     * 
     * @param tree
     * @param splinterNumber
     */
    private List<TreeNode<T>> findLeafNodes(TreeNode<T> tree, int splinterNumber) 
    {
        List<TreeNode<T>> leaves = new ArrayList<TreeNode<T>>();
        
        if (tree.getChildLeft() == null)
        {
            leaves.add(tree);
        }
        else if (tree.getChildLeft().getSplitNumber() > splinterNumber)
        {
            leaves.add(tree);
        }
        else
        {
            leaves.addAll(findLeafNodes(tree.getChildLeft(), splinterNumber) );
            leaves.addAll(findLeafNodes(tree.getChildRight(), splinterNumber) );
        }
        return leaves;
    }
    
    
    
    /**
     * Used by the Diana.PriorityQueue
     */
    public int compare(TreeNode<T> node1, TreeNode<T> node2)
    {
        if (node1.getError() < node2.getError()) return -1;
        if (node1.getError() > node2.getError()) return 1;
        return 0;
    }
    
    
    
    //////////////// Printing ///////////////////
    
    /**
     * Prints out this tree node
     * @return The sting printed. To allow saving the output to file.
     */
    public String print() 
    {
        String str = "";
        for (DataItem value : data)
        {
            str = str + ", " + value.getId();
            System.out.print(", " + value.getId());
        }
        str = str + " : made on " + splitNumber;
        if (childLeft != null)
        {
            if (toBeSplitOn.size() > 0)
            {
                str = str + ", child started using " + getSplitHappenedOnDataId() + " the " + (getSplitHappenedNthFurthest()+1) + " furthest item";
            }
        }
        else if ((parent != null) && (toBeSplitOn.size() == 0))//Bottom-up was used
        {
            str = str + ", created using the " + (splitHappenedOnIndex+1) + " nearest items";
        }
        str = str + "\n";
        System.out.println(" : " + splitNumber);
        return str;
    }
    
    /**
     * Prints this tree node, then the child tree nodes
     * @return The sting printed. To allow saving the output to file.
     */
    public String printTree()
    {
        String str = "";
        Queue<TreeNode> queue = new LinkedList<TreeNode>();
        queue.add(this);
        while(!queue.isEmpty())
        {
            TreeNode r = queue.remove(); 
            str = str + r.print();
            if (r.getChildLeft() != null)
            {
                queue.add(r.getChildLeft());
                queue.add(r.getChildRight());
            }
        }
        return str;
    }
    
    
    /**
     * Gets the id of the data the split happened on
     */
    public String getSplitHappenedOnDataId()
    {
        return data.get(getSplitHappenedNthFurthest()).getId();
    }
    
    /**
     * The splitHappenedOnIndex is the index of the item in data,
     *      when showing the results we want to know if this was the furthest item 
     *          (to show the point of using the n furthest points)
     */
    public int getSplitHappenedNthFurthest()
    {
        for(int i = 0; i < toBeSplitOn.size(); i++)
        {
            if(toBeSplitOn.get(i).equals(splitHappenedOnIndex))
            {
                return i;
            }
        }
        return -1;
    }
    
    public void setSplitHappenedOn(int splitHappenedOnIndex)
    {
        this.splitHappenedOnIndex = splitHappenedOnIndex;
    }
    
    public int getSplitHappenedOn()
    {
        return splitHappenedOnIndex;
    }
    
    //////////////////////////////////////
     
    /*public void removeHigherSplitNodes(TreeNode<T> root)
    {
        if ((root.getChildLeft() != null) && (root.getChildLeft().getSplitNumber() > this.splitNumber))
        {
            System.out.println("Set null");
            root.setChildren(null, null);
        }
        else if (root.getChildLeft() != null)
        {
            removeHigherSplitNodes(root.getChildLeft());
            removeHigherSplitNodes(root.getChildRight());
        }
    } */  
}