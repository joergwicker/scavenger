package scavenger.demo.clustering;

import scavenger.demo.clustering.distance.*;
import scavenger.demo.clustering.errorCalculation.*;
import scavenger.demo.clustering.enums.*;
import scavenger.*;
import scavenger.app.ScavengerAppJ;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Date;
import java.util.Calendar;

import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.Future;

import akka.dispatch.Futures;
import akka.util.Timeout;
import static akka.dispatch.Futures.future;
import static akka.dispatch.Futures.sequence;
import akka.dispatch.*;

/**
 * Performs hierarchical clustering using scavenger
 * Attempts to remove the issue of outliers by trying n possible nodes as the node which starts the splinter cluster.
 *
 */
public class BottomUp<T> extends Diana 
{
    private TreeNodeList<T> bestResult = null; 
    private TreeNodeList<T> currentResult = null; 
     
    /////// constructors : all call super constructor ///////  
    /**
     * @param distanceMeasure The distance measure to be used on all data items.
     */
    public BottomUp(DistanceMeasure distanceMeasure)
    {
        super(distanceMeasure);
    }
    
    /**
     * @param distanceMeasure The distance measure (with weighting) used on all data items.
     */
    public BottomUp(DistanceMeasureSelection distanceMeasureSelection)
    {
        super(distanceMeasureSelection);
    }
    
    /**
     * @param distanceMeasureSelection A list of the different distance measures with weightings. (@see DistanceMeasureSelection)
     */
    public BottomUp(List<DistanceMeasureSelection> distanceMeasureSelection)
    {
        super(distanceMeasureSelection);
    }
    ////////////////////////////////////////////////////////

    /**
     *
     * @param root The root TreeNode that contains all the data to be clusted.
     * @param numberOfIterations The number of times the cluster should be split up 
     *
     * @return The root for the tree of clusters.
     */
    public TreeNodeList<T> runClustering(TreeNodeList<T> root) //TODO why multiple masters start up
    {
        this.setDefaults();        
        int startNumberOfTreeNodes = root.getTreeNodeData().size();        
        startScavenger();
        dianaDistanceFunctions = new DianaDistanceFunctions(dataInfo, numberOfStartSplinterNodes, diameterMeasure);  
        dianaDistanceFunctions.setTrimmedMeanPercent(trimmedMeanPercent);
        //dianaDistanceFunctions.setScavengerContext(scavengerContext());              
     
        PriorityQueue<TreeNodeList<T>> results = new PriorityQueue<TreeNodeList<T>>(1, new TreeNodeList<T>());
        numJobs = 0;
        smallestError = Double.MAX_VALUE;
        isClustered = false;
        
        root.setJoinNodes(dianaDistanceFunctions.getJoinNodes(root.getTreeNodeData(), numberOfStartSplinterNodes, startNumberOfTreeNodes));
        results.add(root);
        
        System.out.println("BottomUp.runClustering() running clustering");
        
        // For all results
        //      For all nodes the new splinter cluster can be started on 
        //          create a Future which performs the splitting of the cluster
        //
        // The TreeNode returned, from the future, is the next node to be splintered (the node with the largest diameter)         
        List<Future<TreeNodeList<T>>> futures = new ArrayList<Future<TreeNodeList<T>>>();  
        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, runTimeSeconds);
        Date endTime = calendar.getTime();
        while (!isClustered && endTime.after(new Date()) && ((results.size() != 0) || (numJobs != 0)))
        {                            
            if(results.size() > 0)  
            {
                TreeNodeList<T> result = results.poll();                
                for(int i = 0; i < result.getJoinNodes().size(); i++)
                {
                    numJobs = numJobs + 1;
                    
                    ScavengerFunction<TreeNodeList<T>> run = new CreateNewJoin(i, dianaDistanceFunctions, numberOfClusters, startNumberOfTreeNodes);
                    Algorithm<TreeNodeList<T>, TreeNodeList<T>> algorithm = scavengerAlgorithm.expensive("CreateNewJoin", run);
                    Computation<TreeNodeList<T>> computation = scavengerComputation.apply("node_"+result+result.getJoinNodes().get(i), result);                    
                    
                    Computation<TreeNodeList<T>> applyComputation = algorithm.apply(computation).cacheGlobally();
                    Future future = scavengerContext().submit(applyComputation);
                    
                    future.onSuccess(new OnSuccess<TreeNodeList<T>>() 
                                     {
                                         public void onSuccess(TreeNodeList<T> result) 
                                         {
                                             setIsClustered(result); 
                                             results.add(result);
                                             decrementNumberOfJobs();
                                         }
                                     }, scavengerContext().executionContext());
                }
            }
            this.handleKeyboardInput();  
        }
        
        System.out.println("Finished");
        System.out.println("Smallest error : " + bestResult.getError());
        return bestResult;
    }
    

    @Override
    protected void handleResults()
    {
        resultHandler.handleResults(bestResult);
    }

    /**
     *
     */
    private void setIsClustered(TreeNodeList<T> result)
    {       
        List<TreeNode<T>> leaves = result.getTreeNodeData();
        
        isClustered = errorCalculation.isClustered(leaves, dianaDistanceFunctions);
        result.setError(errorCalculation.getLastError());
        
        if (bestResult == null)
        {
            bestResult = result;
        }
        else if ((result.getTreeNodeData().size() < bestResult.getTreeNodeData().size()) || (result.getError() < bestResult.getError()))//(result.getTreeNodeData().size() >= bestResult.getTreeNodeData().size()) && 
        {
            bestResult = result;
        }
    }
}



