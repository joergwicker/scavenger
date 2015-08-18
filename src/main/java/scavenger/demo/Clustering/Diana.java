package scavenger.demo.clustering.distance;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;


class Diana<T> extends DianaDistanceFunctions
{
    public Diana(DistanceMeasure distanceMeasure)
    {
        DataInformation dataInfo = new DataInformation("auto", distanceMeasure, 1);
        this.dataInfo = new DataInformation[]{dataInfo};
    }
    
    public Diana(DataInformation dataInfo)
    {
        this.dataInfo = new DataInformation[]{dataInfo};
    }
    
    public Diana(List<DataInformation> dataInfo)
    {
        this.dataInfo = dataInfo.toArray(new DataInformation[dataInfo.size()]);
    }
    
    // TODO create scavenger jobs
    public TreeNode<T> runClustering(TreeNode<T> root, int numberOfIterations)
    {
        System.out.println("runClustering called");
        if(root.getData().size() <= 1)
        {
            System.out.println("Warning : <=1 item given");
            return root;
        }
        
        System.out.println("runClusteringSimple called");
        List<TreeNode<T>> leafNodes = new ArrayList<TreeNode<T>>();//could use depth/breadth first search, but more effiecent just to keep list of leaf nodes
        leafNodes.add(root);
        
        for (int i = 0; i < numberOfIterations; i++)
        {
            double largestDiameter = 0.0;
            int largestDiameterIndex = 0;
            for(int j = 0; j < leafNodes.size(); j++)
            {
                double diameter = calculateClusterDiameter(leafNodes.get(j).getData());
                System.out.println("diameter = " + diameter);
                if (diameter > largestDiameter)
                {
                    largestDiameter = diameter;
                    largestDiameterIndex = j;
                }
            }
            TreeNode node = createNewSpliter(leafNodes.remove(largestDiameterIndex)); 
            leafNodes.add(node.getChildLeft());
            leafNodes.add(node.getChildRight());
        }
        return root;
    }

    
    
    private TreeNode createNewSpliter(TreeNode<T> parent)
    {
        System.out.println("createNewSpliter called");
        List<DataItem<T>> data = parent.getData();
        
        List<DataItem<T>> leftLeaf = new ArrayList<DataItem<T>>();
        List<DataItem<T>> rightLeaf = new ArrayList<DataItem<T>>();
        for (DataItem item : data)
        {
            rightLeaf.add(item);
        }
    
        // find object with highest average distance        
        int indexOfHighestAverage = getIndexWithHighestAverageIndex(rightLeaf);
        
        
        // add indexOfHighestAverage to leftLeaf and rm from rightLeaf
        leftLeaf.add(rightLeaf.remove(indexOfHighestAverage));
        
        // for all items in rightLeaf see if closer to leftLeaf
        for (int i = 0; i < data.size()-1; i++)
        {
            int rightLeafItemIndex = (i - ((data.size()-1) - rightLeaf.size()));
            double avarageRight = calculateAverage(rightLeaf, rightLeafItemIndex);
            leftLeaf.add(rightLeaf.get(rightLeafItemIndex));
            double avarageLeft = calculateAverage(leftLeaf, leftLeaf.size()-1);
            
            if (avarageLeft < avarageRight)
            {
                rightLeaf.remove(rightLeafItemIndex);
            }
            else
            {
                leftLeaf.remove(leftLeaf.size()-1);
            }
        }
        
        TreeNode<T> leftTreeNode = new TreeNode<T>(leftLeaf, parent);
        TreeNode<T> rightTreeNode = new TreeNode<T>(rightLeaf, parent);
        parent.setChildren(leftTreeNode, rightTreeNode);
        
        //System.out.println("createNewSpliter : leftTreeNode " + leftLeaf.size() + " rightTreeNode " + rightLeaf.size());
        
        return parent;
    }
    
}


