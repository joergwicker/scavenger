package scavenger.demo.clustering.distance;

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
 * Performs Diana (DIvisive ANAlysis) clustering
 *
 * //TODO better way of making use of scavenger
 *
 * @see DianaDistanceFunctions
 */
class Diana<T> extends DianaDistanceFunctions
{
    /////// Constructors ///////
    
    /**
     * 
     * @param distanceMeasure The distance measure to be used on all data items.
     */
    public Diana(DistanceMeasure distanceMeasure)
    {
        super();
        DistanceMeasureSelection dataInfo = new DistanceMeasureSelection("auto", distanceMeasure, 1);
        this.dataInfo = new DistanceMeasureSelection[]{dataInfo};
    }
    
    /**
     * @param distanceMeasure The distance measure (with weighting) used on all data items.
     */
    public Diana(DistanceMeasureSelection distanceMeasureSelection)
    {
        super();
        this.dataInfo = new DistanceMeasureSelection[]{distanceMeasureSelection};
    }
    
    /**
     * @param distanceMeasureSelection A list of the different distance measures with weightings. (@see DistanceMeasureSelection)
     */
    public Diana(List<DistanceMeasureSelection> distanceMeasureSelection)
    {
        super();
        this.dataInfo = distanceMeasureSelection.toArray(new DistanceMeasureSelection[distanceMeasureSelection.size()]);
    }
    
    ///////////////////////////
    
    /**
     *
     * @param root The root TreeNode that contains all the data to be clusted.
     * @param numberOfIterations The number of times the cluster should be split up 
     *
     * @return The root for the tree of clusters.
     */
    public TreeNode<T> runClustering(TreeNode<T> root, int numberOfIterations)
    {
        System.out.println("runClustering called");
        if(root.getData().size() <= 1)
        {
            System.out.println("Warning : <=1 items given");
            return root;
        }
        
        List<TreeNode<T>> leafNodes = new ArrayList<TreeNode<T>>();
        leafNodes.add(root);
        
        for (int i = 0; i < numberOfIterations; i++)
        {           
            int largestDiameterIndex = getClusterIndexWithLargestDiameter(leafNodes);
            //TreeNode node = createNewSpliter(leafNodes.remove(largestDiameterIndex)); 
            TreeNode node = leafNodes.remove(largestDiameterIndex); // will no longer be a leaf node
            node = runCreateNewSpliterJob(node); 
            leafNodes.add(node.getChildLeft());
            leafNodes.add(node.getChildRight());
        }
        return root;
    }

    /**
     * Finds the cluster with the largest diameter 
     * 
     * @param clusters A list of the TreeNodes who's diameter will be checked
     * @return index of the cluster with the largest diameter
     */
    private int getClusterIndexWithLargestDiameter(List<TreeNode<T>> clusters)
    {
        double largestDiameter = 0.0;
        int largestDiameterIndex = 0;
        for(int j = 0; j < clusters.size(); j++)
        {
            double diameter = calculateClusterDiameter(clusters.get(j).getData());
            //System.out.println("diameter = " + diameter);
            if (diameter > largestDiameter)
            {
                largestDiameter = diameter;
                largestDiameterIndex = j;
            }
        }
        return largestDiameterIndex;
    }

    // TODO Has been setup to use scavenger, but at the moment pointless as we always wait for a single future to return
    private TreeNode<T> runCreateNewSpliterJob(TreeNode<T> parent)
    {
        ScavengerFunction<TreeNode<T>> run = new CreateNewSpliter<T>();
        Computation<TreeNode<T>> computationData = scavengerComputation.apply("parent", parent).cacheGlobally();
        Algorithm<TreeNode<T>, TreeNode<T>> algorithm = scavengerAlgorithm.expensive("createNewSpliter", run).cacheGlobally();
        Computation<TreeNode<T>> computation1 = algorithm.apply(computationData);
        Future<TreeNode<T>> future = scavengerContext().submit(computation1);
        
        try
        {
            parent = (TreeNode<T>)Await.result(future, (new Timeout(Duration.create(40, "seconds")).duration()));
        }
        catch(Exception e) 
        { 
            e.printStackTrace(); 
        }
        return parent;
    }
    
    
    /**
     * Finds object with highest average distance. This object becomes the start of the new cluster (leftLeaf).
     * If an item is closure to the new cluster (leftLeaf), than the old cluster (rightLeaf) :
     *       remove it from the old cluster (rightLeaf), and add it to the new cluster (leftLeaf).
     *
     * @param parent The TreeNode containing cluster to be split up
     * @return The parent TreeNode with it's children set to the TreeNodes containing the new clusters.
     */
    class CreateNewSpliter<T> extends ScavengerFunction<TreeNode<T>> 
    {
        public TreeNode<T> call()
        { 
            TreeNode parent = value;
            System.out.println("createNewSpliter called");
            List<DataItem<T>> data = parent.getData();
            
            List<DataItem<T>> leftLeaf = new ArrayList<DataItem<T>>();
            List<DataItem<T>> rightLeaf = new ArrayList<DataItem<T>>();
            for (DataItem<T> item : data)
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
    };
   
   public void endClustering()
   {
      scavengerShutdown();  
   }
   
   /*
    private TreeNode<T> createNewSpliter(TreeNode<T> parent)
    { 
        TreeNode parent = value;
        System.out.println("createNewSpliter called");
        List<DataItem> data = parent.getData();
        
        List<DataItem> leftLeaf = new ArrayList<DataItem>();
        List<DataItem> rightLeaf = new ArrayList<DataItem>();
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
        
        TreeNode leftTreeNode = new TreeNode(leftLeaf, parent);
        TreeNode rightTreeNode = new TreeNode(rightLeaf, parent);
        parent.setChildren(leftTreeNode, rightTreeNode);
        
        //System.out.println("createNewSpliter : leftTreeNode " + leftLeaf.size() + " rightTreeNode " + rightLeaf.size());
        
        return parent;
      
    }*/
    
}


