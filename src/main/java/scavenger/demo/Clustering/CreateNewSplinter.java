package scavenger.demo.clustering;

import scavenger.demo.clustering.distance.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

import scavenger.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.Future;

import akka.dispatch.Futures;
import akka.util.Timeout;
import static akka.dispatch.Futures.future;
import static akka.dispatch.Futures.sequence;


/*
 * Finds object with highest average distance. This object becomes the start of the new cluster (leftLeaf).
 * If an item is closure to the new cluster (leftLeaf), than the old cluster (rightLeaf) :
 *       remove it from the old cluster (rightLeaf), and add it to the new cluster (leftLeaf).
 *
 * @param parent The TreeNode containing cluster to be split up
 * @return The parent TreeNode with it's children set to the TreeNodes containing the new clusters.
 */
public class CreateNewSplinter<T> extends ScavengerFunction<TreeNode<T>> 
{
    private DianaDistanceFunctions dianaDistanceFunctions;
    private int splinterStartNode;
    
    /**
     *
     * @param splinterStartNode
     * @param dianaDistanceFunctions
     */
    public CreateNewSplinter(int splinterStartNode, DianaDistanceFunctions dianaDistanceFunctions)
    {
        this.splinterStartNode = splinterStartNode;
        this.dianaDistanceFunctions = dianaDistanceFunctions;
    }
    
    
    public TreeNode<T> call()
    { 
        //dianaDistanceFunctions.setScavengerContext(ctx);
        
        TreeNode parent = value;
        
        //System.out.println("CreateNewSplinter.call() called");
        List<DataItem<T>> data = parent.getData();
        
        List<DataItem<T>> leftLeaf = new ArrayList<DataItem<T>>();
        List<DataItem<T>> rightLeaf = new ArrayList<DataItem<T>>();
        for (DataItem<T> item : data)
        {
            rightLeaf.add(item);
        }
        
        // find object with highest average distance        
        //int indexOfHighestAverage = splinterStartNode;        
        
        // add indexOfHighestAverage to leftLeaf and rm from rightLeaf
        leftLeaf.add(rightLeaf.remove(splinterStartNode));
        
        // for all items in rightLeaf see if closer to leftLeaf
        for (int i = 0; i < data.size()-1; i++)
        {
            int rightLeafItemIndex = (i - ((data.size()-1) - rightLeaf.size()));
            double avarageRight = dianaDistanceFunctions.calculateAverage(rightLeaf, rightLeafItemIndex);
            leftLeaf.add(rightLeaf.get(rightLeafItemIndex));
            double avarageLeft = dianaDistanceFunctions.calculateAverage(leftLeaf, leftLeaf.size()-1);
            
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
        leftTreeNode.setUpSplinterInfo(dianaDistanceFunctions.getIndexFurthestPoints(leftTreeNode));
        rightTreeNode.setUpSplinterInfo(dianaDistanceFunctions.getIndexFurthestPoints(rightTreeNode));
        //System.out.println("createNewSplinter : leftTreeNode " + leftLeaf.size() + " rightTreeNode " + rightLeaf.size());            
        
        List<TreeNode<T>> leafNodes = dianaDistanceFunctions.findLeafNodes(parent.getRoot());//dianaDistanceFunctions.findRoot(parent));
        System.out.println("CreateNewSplinter leafNodes " + leafNodes.size());
        int largestDiameterIndex = dianaDistanceFunctions.getClusterIndexWithLargestDiameter(leafNodes);            
        return leafNodes.get(largestDiameterIndex);
        

    }
}

