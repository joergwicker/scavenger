package scavenger.demo.clustering.bottomUp;

import scavenger.demo.clustering.distance.*;
import scavenger.demo.clustering.*;

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


/**
 * Used for bottom up clustering to join two clusters together. 
 *
 */
public class CreateNewJoin<T> extends ScavengerFunction<TreeNodeList<T>> 
{
    private DianaDistanceFunctions dianaDistanceFunctions;
    private int joinNodeIndex;
    private int numberOfClusters;
    private int startNumberOfTreeNodes;
    
    /**
     *
     * @param joinNodeIndex
     * @param dianaDistanceFunctions
     * @param numberOfClusters The number of clusters that should be created
     * @param startNumberOfTreeNodes The number of clusters started with
     */
    public CreateNewJoin(int joinNodeIndex, DianaDistanceFunctions dianaDistanceFunctions, int numberOfClusters, int startNumberOfTreeNodes)
    {
        this.joinNodeIndex = joinNodeIndex;
        this.dianaDistanceFunctions = dianaDistanceFunctions;
        this.numberOfClusters = numberOfClusters;
        this.startNumberOfTreeNodes = startNumberOfTreeNodes;
    }
    
    /**
     * @return The TreeNodeList containing a list of all the clusters
     */
    public TreeNodeList<T> call()
    {         
        TreeNodeList<T> child = value;
        
        Integer[] joinIndexes = child.getJoinNodes().get(joinNodeIndex);
        List<TreeNode<T>> leaves = child.getTreeNodeData();
        
        // get the data from the items being joined
        List<DataItem<T>> newTreeNodeData = new ArrayList<DataItem<T>>();
        newTreeNodeData.addAll(leaves.get(joinIndexes[0]).getData());
        newTreeNodeData.addAll(leaves.get(joinIndexes[1]).getData());
        
        // Create the TreeNode and set the parent and children nodes
        TreeNode<T> newNode = new TreeNode(newTreeNodeData, startNumberOfTreeNodes-leaves.size());
        TreeNode<T> left = leaves.get(joinIndexes[0]);
        left.setParent(newNode);
        TreeNode<T> right = leaves.get(joinIndexes[1]);
        right.setParent(newNode);
        newNode.setChildren(left, right);
        newNode.setSplitHappenedOn(joinNodeIndex);
        
        // Create the list of all current leaf nodes
        List<TreeNode<T>> newLeaves = new ArrayList<TreeNode<T>>();
        for(int i = 0; i < leaves.size(); i++)
        {
            if ((!joinIndexes[0].equals(i)) && (!joinIndexes[1].equals(i)))
            {
                TreeNode<T> treeNode = leaves.get(i);
                newLeaves.add(treeNode);
            }
        }
        newLeaves.add(newNode);
        List<Integer[]> joinNodes = dianaDistanceFunctions.getJoinNodes(newLeaves, numberOfClusters, startNumberOfTreeNodes);
        return new TreeNodeList(newLeaves, joinNodes);
    }
}

