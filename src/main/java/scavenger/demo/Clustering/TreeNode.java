package scavenger.demo.clustering.distance;

import java.util.List;

class TreeNode<T>
{
    private List<DataItem<T>> data;
    private TreeNode parent;
    private TreeNode childLeft = null;
    private TreeNode childRight = null;

    public TreeNode(List<DataItem<T>> data)
    {
        this.data = data;
    }
    
    public TreeNode(List<DataItem<T>> data, TreeNode parent)
    {
        this.data = data;
        this.parent = parent;
    }
    
    public List<DataItem<T>> getData()
    {
        return data;
    }
    
    public void setChildren(TreeNode childLeft, TreeNode childRight)
    {
        this.childLeft = childLeft;
        this.childRight = childRight;
    }
    
    
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