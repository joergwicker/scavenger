package scavenger.demo.clustering;
import scavenger.demo.clustering.distance.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
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
    private int splitHappenedOnIndex = 0;
    
    private double error = 1;

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
    
    public void setUpSplinterInfo(List<Integer> toBeSplitOn, int numberOfSplinters)
    {
        
        splitNumber = countSplits(getRoot());
        System.out.println("numberOfSplinters : " + numberOfSplinters );
        System.out.println("splitNumber : " + splitNumber );
        if ((numberOfSplinters <= 0) || (splitNumber < numberOfSplinters))
        {
            System.out.println("setUpSplinterInfo() true" );
            this.toBeSplitOn = toBeSplitOn;
        }
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
    
    
    public void setSplitHappenedOn(int splitHappenedOnIndex)
    {
        this.splitHappenedOnIndex = splitHappenedOnIndex;
    }
    
    public int getSplitHappenedOn()
    {
        return splitHappenedOnIndex;
    }
    
    public String getSplitHappenedOnDataId()
    {
        return data.get(getSplitHappenedOnIndex()).getId();
    }
    
    public int getSplitHappenedOnIndex()
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
    
    public String print() 
    {
        String str = "";
        for (DataItem value : data)
        {
            str = str + ", " + value.getId();
            System.out.print(", " + value.getId());
        }
        str = str + " : made on split number " + splitNumber;
        if (childLeft != null)
        {
            str = str + ", child started using " + getSplitHappenedOnDataId() + " the " + (getSplitHappenedOnIndex()+1) + " furthest item";
        }
        str = str + "\n";
        System.out.println(" : " + splitNumber);
        return str;
    }
    
    
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
    
    
    public List<TreeNode<T>> findLeafNodes()
    {
        return findLeafNodes(this);
    }
    
    public List<TreeNode<T>> findLeafNodes(int splinterNumber)
    {
        return findLeafNodes(this, splinterNumber);
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