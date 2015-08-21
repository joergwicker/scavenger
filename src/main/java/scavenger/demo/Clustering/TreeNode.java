package scavenger.demo.clustering.distance;

import java.util.List;

/**
 * A node within the cluster hierarchy.
 */
class TreeNode<T> implements java.io.Serializable
{
    private List<DataItem<T>> data;
    private TreeNode parent;
    private TreeNode childLeft = null;
    private TreeNode childRight = null;

    /**
     * Constructor for the root node.
     *
     * @param data The DataItems being clustered
     */
    public TreeNode(List<DataItem<T>> data)
    {
        this.data = data;
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
    
    /**
     * @return 
     */ 
    public List<DataItem<T>> getData()
    {
        return data;
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
    
    
    public void print() 
    {
        for (DataItem value : data)
        {
            System.out.print(", " + value.getId());
        }
        System.out.println(" ");
    }
    
}