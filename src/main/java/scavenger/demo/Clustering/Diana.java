package scavenger.demo.clustering;

import scavenger.demo.clustering.distance.*;
import scavenger.demo.clustering.errorCalculation.*;
import scavenger.demo.clustering.enums.*;
import scavenger.demo.clustering.resultHandler.*;
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

import java.lang.Throwable;

/**
 * Performs Diana (DIvisive ANAlysis) clustering using scavenger
 * Attempts to remove the issue of outliers by trying n possible nodes as the node which starts the splinter cluster.
 *
 */
public class Diana<T> extends ScavengerAppJ
{
    protected DianaDistanceFunctions dianaDistanceFunctions;    
    protected DistanceMeasureSelection[] dataInfo;
    
    protected int runTimeSeconds = 0;
    protected int numberOfStartSplinterNodes = 0;
    protected int numberOfClusters = 0;
    protected int timeoutSeconds = 60;//timeout for a single job 
    protected ResultHandler resultHandler = null;
    
    protected DiameterMeasure diameterMeasure = null;
    protected List<Integer> trimmedMeanPercent = new ArrayList<Integer>();
    
    protected double errorThreshold = 0.0;//0.04  
    protected ErrorCalculation<T> errorCalculation = null; // Are the clusters "good" clusters?
    
    
    private TreeNode<T> bestResult = null; 
    protected double smallestError = Double.MAX_VALUE;
    protected boolean isClustered = false;
    protected int numJobs = 0;
    
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
    
    ////// setters ///////////
    
    public void setErrorCalculation(ErrorCalculation<T> errorCalculation)
    {
        this.errorCalculation = errorCalculation;
    }
    
    public void setErrorThreshold(double errorThreshold)
    {
        this.errorThreshold = errorThreshold;
    }
    
    public void setRunTimeSeconds(int runTimeSeconds)
    {
        this.runTimeSeconds = runTimeSeconds;
    }
    
    public void setDiameterMeasure(DiameterMeasure diameterMeasure)
    {
        this.diameterMeasure = diameterMeasure;
    }
    
    public void setNumberOfStartSplinterNodes(int numberOfStartSplinterNodes)
    {
        this.numberOfStartSplinterNodes = numberOfStartSplinterNodes;
    }
    
    public void setNumberOfClusters(int numberOfClusters)
    {
        this.numberOfClusters = numberOfClusters;
    }
    
    public void setTimeoutSeconds(int timeoutSeconds)
    {
        this.timeoutSeconds = timeoutSeconds;
    }
    
    public void setResultHandler(ResultHandler resultHandler)
    {
        this.resultHandler = resultHandler;
    }
    
    public void setTrimmedMeanPercent(List<Integer> trimmedMeanPercent)
    {
        this.trimmedMeanPercent = trimmedMeanPercent;
    }
    
    public void setTrimmedMeanPercent(int trimmedMeanPercent)
    {
        this.trimmedMeanPercent = new ArrayList<Integer>();
        this.trimmedMeanPercent.add(trimmedMeanPercent);
    }
    ///////////////////////////
    
    /**
     * 
     */
    protected void setDefaults()
    {
        System.out.println("Diana.runClustering() called");
        System.out.println("Help : ");
        System.out.println("    p : Prints current result");
        System.out.println("    q : Stops clustering and returns current result \n");
        
        System.out.println("Diana : setting defaults ");
        if (errorCalculation == null)
        {
            System.out.println("ErrorCalculation has not been set, using SimpleErrorCalculation with errorThreshold of " + errorThreshold);
            this.errorCalculation = new SimpleErrorCalculation(errorThreshold);
        }
        if (runTimeSeconds == 0)
        {
            System.out.println("runTimeSeconds has not been set, using default (30)");
            runTimeSeconds = 30; 
        }
        if (numberOfStartSplinterNodes == 0)
        {
            System.out.println("numberOfStartSplinterNodes has not been set, using default (3)");
            numberOfStartSplinterNodes = 3; 
        }
        if (diameterMeasure == null)
        {
            System.out.println("diameterMeasure has not been set, using default (DiameterMeasure.TRIMMED_MEAN)");
            diameterMeasure = DiameterMeasure.TRIMMED_MEAN;//LARGEST_AVERAGE_DISTANCE; 
        }
        if((trimmedMeanPercent.size() == 0) && (diameterMeasure == DiameterMeasure.TRIMMED_MEAN))
        {
            System.out.println("trimmedMeanPercent has not been set, using default (5)");
            trimmedMeanPercent.add(5);
        }
    }
    
    /**
     *
     * @param root The root TreeNode that contains all the data to be clusted.
     * @param numberOfIterations The number of times the cluster should be split up 
     *
     * @return The root for the tree of clusters.
     */
    public TreeNode<T> runClustering(TreeNode<T> root) 
    {    
        if(root.getData().size() <= 1)
        {
            System.out.println("Warning : <=1 items given");
            return root;
        }
        this.setDefaults();
        
        startScavenger();
        dianaDistanceFunctions = new DianaDistanceFunctions(dataInfo, numberOfStartSplinterNodes, diameterMeasure);  
        dianaDistanceFunctions.setTrimmedMeanPercent(trimmedMeanPercent);
        //dianaDistanceFunctions.setScavengerContext(scavengerContext());                      
        
        PriorityQueue<TreeNode<T>> results = new PriorityQueue<TreeNode<T>>(1, new TreeNode<T>());
        numJobs = 0;
        smallestError = Double.MAX_VALUE;
        isClustered = false;
        
        root.setToBeSplitOn(dianaDistanceFunctions.getIndexFurthestPoints(root));
        results.add(root);
        
        System.out.println("Diana : running clustering");
        
        // For all results 
        //      For all nodes the new splinter cluster can be started on 
        //          create a Future which performs the splitting of the cluster
        //
        // The TreeNode returned, from the future, is the next node to be splintered (the node with the largest diameter)         
        List<Future<TreeNode<T>>> futures = new ArrayList<Future<TreeNode<T>>>();  
        Calendar calendar = Calendar.getInstance(); 
        calendar.add(Calendar.SECOND, runTimeSeconds);
        Date endTime = calendar.getTime();
        while (!isClustered && endTime.after(new Date()) && ((results.size() != 0) || (numJobs != 0)))
        {      
            if(results.size() > 0)  
            {
                TreeNode<T> result = results.poll();                
                for(int i = 0; i < result.getToBeSplitOn().size(); i++)
                {
                    numJobs = numJobs + 1;
                    ScavengerFunction<TreeNode<T>> run = new CreateNewSplinter(result.getToBeSplitOn().get(i), dianaDistanceFunctions, numberOfClusters);
                    Algorithm<TreeNode<T>, TreeNode<T>> algorithm = scavengerAlgorithm.expensive("createNewSplinter", run);
                    Computation<TreeNode<T>> computation = scavengerComputation.apply("node_"+result+result.getToBeSplitOn().get(i), result);                    
                    
                    Computation<TreeNode<T>> applyComputation = algorithm.apply(computation).cacheGlobally();
                    Future future = scavengerContext().submit(applyComputation);
                    
                    future.onSuccess(new OnSuccess<TreeNode<T>>() 
                                     {
                                         public void onSuccess(TreeNode<T> currentResult) 
                                         {
                                             setIsClustered(currentResult); 
                                             results.add(currentResult);
                                             decrementNumberOfJobs();
                                         }
                                     }, scavengerContext().executionContext());
                }
            }              
            this.handleKeyboardInput(); 
        }
        
        System.out.println("Finished");
        System.out.println("Smallest error : " + smallestError);
        return bestResult.getRoot();
    }
    
    /**
     *  Called from OnSuccess. As OnSuccess is an inner class it should not modify the outer class attributes.
     * 
     */
    protected void decrementNumberOfJobs()
    {
        numJobs = numJobs - 1;
    }
    
    /**
     * Allows user to ask for results and for the clustering to finish
     */
    public void handleKeyboardInput()
    {
        try 
        {
            //Thread.sleep(10); 
            Thread.yield();
            if(System.in.available() > 0)
            {
                int keyboardInput = System.in.read();
                if ((keyboardInput == (int)'p') && (resultHandler != null))
                {
                    //resultHandler.handleResults(bestResult.getRoot());
                    handleResults();
                }
                else if (keyboardInput == (int)'q')
                {
                    isClustered = true;
                }
            }
        }
        catch(Exception e) 
        { 
            e.printStackTrace(); 
        }
    }
    
    /**
     *
     */
    protected void handleResults()
    {
        resultHandler.handleResults(bestResult.getRoot());
    }
    
       
    /**
     * Checks if the clustering has been completed
     *
     * @param results The list returned by the list of futures.
     */
    private void setIsClustered(TreeNode<T> result)//List<TreeNode<T>> results)
    {        
        TreeNode<T> root = result.getRoot();//dianaDistanceFunctions.findRoot(result);
        List<TreeNode<T>> leaves = root.findLeafNodes();
        //System.out.println("Number of leaves : " + leaves.size());
        
        isClustered = errorCalculation.isClustered(leaves, dianaDistanceFunctions);
        if (numberOfClusters > 0)
        {
            if (leaves.size() != numberOfClusters)
            {
                isClustered = false;
            }
        }
        //System.out.println("errorCalculation.getLastError(): " + errorCalculation.getLastError());
        /*if (errorCalculation.getLastError() <= smallestError)
        {
            smallestError = errorCalculation.getLastError();
            bestResult = result;
        }*/
        result.setError(errorCalculation.getLastError());
        
        
        if (bestResult == null)
        {
            bestResult = result;
        }
        else if ((leaves.size() > bestResult.getRoot().findLeafNodes().size()) || (result.getError() < bestResult.getError())) 
        {
            bestResult = result;
        }
    }
    
    /**
     *
     */
    public void endClustering()
    {
        scavengerShutdown();  
    }
}


