package scavenger.demo.clustering;

import scavenger.demo.clustering.distance.*;
import scavenger.demo.clustering.enums.*;
import scavenger.app.ScavengerAppJ;
import scavenger.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Comparator;

/*import java.util.concurrent.ExecutorService;
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
import static akka.dispatch.Futures.sequence;*/

/**
 *  Holds the average and diameter calculations. (Methods that both Diana and CreateNextSplinter are likely to use)
 *
 */
public class DianaDistanceFunctions<T> implements java.io.Serializable
{
    private DistanceMeasureSelection[] dataInfo;
    private int numberOfStartSplinterNodes = 0;
    
    private DiameterMeasure distanceDiameter = DiameterMeasure.TRIMMED_MEAN;
    private List<Integer> trimmedMeanPercents = new ArrayList<Integer>();
    
    /**
     *
     * @param dataInfo
     * @param numberOfStartSplinterNodes
     * @param distanceDiameter
     */
    public DianaDistanceFunctions(DistanceMeasureSelection[] dataInfo, int numberOfStartSplinterNodes, DiameterMeasure distanceDiameter )
    {
        this.dataInfo = dataInfo;
        this.numberOfStartSplinterNodes = numberOfStartSplinterNodes;
        this.distanceDiameter = distanceDiameter;
        
        trimmedMeanPercents.add(5);
    }
    public DianaDistanceFunctions(){}
    /*public void setTrimmedMeanPercent(int trimmedMeanPercent)
    {
        this.trimmedMeanPercent = trimmedMeanPercent;
    }*/
    
    public void setTrimmedMeanPercent(List<Integer> trimmedMeanPercents)
    {
        this.trimmedMeanPercents = trimmedMeanPercents;
    }
    
    
    /**
     * Finds the cluster with the largest diameter 
     * 
     * @param clusters A list of the TreeNodes who's diameter will be checked
     * @return index of the cluster with the largest diameter
     */
    public int getClusterIndexWithLargestDiameter(List<TreeNode<T>> clusters)
    {
        double largestDiameter = 0.0;
        int largestDiameterIndex = 0;
        for (int j = 0; j < clusters.size(); j++)
        {
            double clusterDiameter = calculateClusterDiameter(clusters.get(j));
            if (clusterDiameter > largestDiameter)
            {
                largestDiameter = clusterDiameter;
                largestDiameterIndex = j;
            }
        }
        return largestDiameterIndex;
        /*
        List<Double> diameters = calculateClusterDiameters(clusters);
        for(int j = 0; j < diameters.size(); j++)
        {
            double diameter = diameters.get(j);
            if (diameter > largestDiameter)
            {
                largestDiameter = diameter;
                largestDiameterIndex = j;
            }
        }
        //System.out.println("largestDiameterIndex : " + largestDiameterIndex);
        return largestDiameterIndex;*/
    }
    
    /**
     * Calculates the diameters of all the given clusters
     * Uses scavenger. Means that if the diameter of a cluster has already been calculated it is not re-calculated.
     * 
     * @param clusters The clusters who's diameters are to be calculated
     * @return the diameters of the clusters
     */
    public List<Double> calculateClusterDiameters(List<TreeNode<T>> clusters)
    {
        List<Double> distances = new ArrayList<Double>(clusters.size());
        
        for(int j = 0; j < clusters.size(); j++)
        {
            distances.add(calculateClusterDiameter(clusters.get(j)));
        }        
        return distances;    
    }
    
    /** 
     * Gets a list of all nodes, which are not leaf nodes.
     * These are the nodes at which a decision has been made
     *
     * @param root
     * @return list of tree nodes
     */
    public List<TreeNode<T>> getNodeListWithoutLeafNodes(TreeNode<T> root)
    {
        List<TreeNode<T>> noneLeafNodes = new ArrayList<TreeNode<T>>();
        
        if (root.getChildLeft() != null)
        {            
            noneLeafNodes.addAll(getNodeListWithoutLeafNodes(root.getChildLeft()));
            noneLeafNodes.addAll(getNodeListWithoutLeafNodes(root.getChildRight()));
            noneLeafNodes.add(root);
        }
        return noneLeafNodes;
    }
    
    
    
    /**
     * Max distance between two elements in a cluster
     * Uses scavenger. Means that if the diameter of a cluster has already been calculated it is not re-calculated.
     * 
     * @param cluster The cluster who's diameter is to be calculated
     * @return the diameter of the cluster
     */
    public double calculateClusterDiameter(TreeNode<T> node)//List<DataItem<T>> cluster)
    {
        List<DataItem<T>> cluster = node.getData();
        if(distanceDiameter == DiameterMeasure.LARGEST_AVERAGE_DISTANCE)
        {
            // This calculates the max average distance
            double maxDistance = 0.0;
            
            for (int i = 0; i < cluster.size(); i++)
            {
                double distance = calculateAverage(cluster, i);
                
                if (distance > maxDistance)
                {
                    maxDistance = distance;
                }
            }    
            return maxDistance;
        }
        else //if(distanceDiameter == DiameterMeasure.TRIMMED_MEAN)
        {            
            // calculates the trimmed mean
            //System.out.println("calculateClusterDiameter cluster.size() : " + cluster.size());
            List<Double> allAverages = new ArrayList<Double>();        
            for (int i = 0; i < cluster.size(); i++)
            {
                double average = calculateAverage(cluster, i); 
                // insert the average using binarySearch (allAverages is a sorted list) 
                int index = java.lang.Math.abs(java.util.Collections.binarySearch(allAverages, average))-1;
                if (index < 0)
                {
                    index = 0;
                }
                allAverages.add(index, average);
            }
            if(allAverages.size() > 1)
            {
                int currentTrimmedMeanPercent = 0;
                if (trimmedMeanPercents.size() == 1)
                {
                    currentTrimmedMeanPercent = trimmedMeanPercents.get(0);
                }
                else
                {
                    if (trimmedMeanPercents.size() > node.getSplitNumber())
                    {
                        currentTrimmedMeanPercent = trimmedMeanPercents.get(node.getSplitNumber());
                    }
                }
                //System.out.println("Using trimmedMeanPercent = " + currentTrimmedMeanPercent);
                if (currentTrimmedMeanPercent > 0)
                {
                    allAverages = allAverages.subList(0, (allAverages.size()/(100/currentTrimmedMeanPercent))+1);
                }
            }
            double totalDistance = 0.0;
            for (Double distance : allAverages)
            {
                totalDistance = totalDistance + distance;
            }
            return totalDistance/allAverages.size();
        }
    }

    
    /**
     * The node returned will start the new cluster
     *
     * @param cluster 
     * @return The index of the item with the highest average distance
     */
    public int getIndexWithHighestAverageIndex(List<DataItem<T>> cluster)
    {
        int indexOfHighestAverage = 0;
        double highestAverege = 0;
        for (int i = 0; i < cluster.size(); i++)
        {
            double average = calculateAverage(cluster, i); 
            if (average > highestAverege)
            {
                indexOfHighestAverage = i;
                highestAverege = average;
            }
        }
        return indexOfHighestAverage;
    }
    
    
    /**
     * Runs calculateAverageSimple if only one distance measure is being used; else runs calculateAverageComplex
     * 
     * @param cluster
     * @param index The index of the item, who's average distance is being calculated
     * @return The average distance
     */ 
    public double calculateAverage(List<DataItem<T>> cluster, int index)
    {
        if (dataInfo.length == 1 )
        {
            return calculateAverageSimple(cluster, index, dataInfo[0].getDistanceMeasure()); 
        }
        else
        {
            return calculateAverageComplex(cluster, index);
        }            
    }
    
    /**
     * Calculates the average distance, when one distance measure is being used.
     *
     * @param cluster
     * @param index The index of the item, who's average distance is being calculated
     * @return The average distance
     */
    public double calculateAverageSimple(List<DataItem<T>> cluster, int index, DistanceMeasure distanceMeasure)
    {
        double total = 0;
        for(int i = 0; i < cluster.size(); i++)
        {
            if (index == i) 
            {
                continue;
            }
            
            total = total + distanceMeasure.getDistance(cluster.get(index).getData(), cluster.get(i).getData()); 
            
            //total = total + getDistance(cluster.get(index).getData(), cluster.get(i).getData(), dataInfo[0].getDistanceMeasure());            
        }
        return total / cluster.size();
    }
    
    
    /**
     * Calculates the average distance, when multiple distance measure is being used.
     *
     * @param cluster
     * @param index The index of the item, who's average distance is being calculated
     * @return The average distance 
     */
    private double calculateAverageComplex(List<DataItem<T>> cluster, int index)
    {
        double total = 0;
        double totalWeightings = 0;
        for(int i = 0; i < cluster.size(); i++)
        {
            double subTotal = 0;
            totalWeightings = 0;
            if (index == i) 
            {
                continue;
            }
            for(DistanceMeasureSelection distanceMeasure : dataInfo)
            {
                for(String id : distanceMeasure.getIds()) 
                {
                    try
                    {
                        subTotal = subTotal + (distanceMeasure.getDistanceMeasure().getDistance(cluster.get(index).getHashMap().get(id), cluster.get(i).getHashMap().get(id)) * distanceMeasure.getWeight());
                       // subTotal = subTotal + getDistance(cluster.get(index).getHashMap().get(id), cluster.get(i).getHashMap().get(id), distanceMeasure.getDistanceMeasure()) * distanceMeasure.getWeight();
                        totalWeightings = totalWeightings + distanceMeasure.getWeight();//1;
                    }
                    catch(Exception ex) 
                    {
                        System.out.println("WARNING : Likely that DistanceMeasureSelection contains a key which is not found in the HashMap of DataItems");
                        ex.printStackTrace();
                    }
                }
            }
            total = total + (subTotal / totalWeightings);
            //System.out.println("subTotal " + subTotal + " / numberOfItems " + totalWeightings + " = " + subTotal / totalWeightings);
        }    
        //System.out.println("total / cluster.size() " + total / cluster.size());    
        return total / cluster.size(); 
    }

    /**
     * Used by getIndexFurthestPoints() to create an ordered list of pairs
     */
    class DistanceData 
    {
        protected DistanceData(Double distance, Integer index)
        {
            this.distance = distance;
            this.index = index;
        }
        Double distance;
        Integer index;
    }
    
    /**
     *
     * @param cluster
     * @return The points with the largest average distance
     */  
    public List<Integer> getIndexFurthestPoints(TreeNode<T> cluster)
    {
        List<Integer> indexesOfHighestAverage = new ArrayList<Integer>();
        List<Double> highestAverages = new ArrayList<Double>();
        //System.out.println("getIndexFurthestPoints cluster.getData() : " + cluster.getData().size());
        List<DistanceData> allAverages = new ArrayList<DistanceData>();
        for (int i = 0; i < cluster.getData().size(); i++)
        {
            
            double average = calculateAverage(cluster.getData(), i); 
            if (allAverages.size() == 0) 
            {
                allAverages.add(new DistanceData(average, i));
            } 
            else if (allAverages.get(0).distance > average) 
            {
                allAverages.add(0, new DistanceData(average, i));
            } 
            else if (allAverages.get(allAverages.size() - 1).distance < average) 
            {
                allAverages.add(allAverages.size(), new DistanceData(average, i));
            } 
            else 
            {
                int j = 0;
                while (allAverages.get(j).distance < average) //TODO make more efficent
                {
                    j++;
                }
                allAverages.add(j, new DistanceData(average, i));
            }
        }
        for (int i = 0; (i < numberOfStartSplinterNodes) && (i < allAverages.size()); i++)
        {
            indexesOfHighestAverage.add(allAverages.get(i).index);    
        }        
        return indexesOfHighestAverage;
    }
    
    
    
    //// Distance calculated using scavenger (so results can be cached). Currently too unreliable
   /* protected transient Context scavengerContext;
    protected package$ scavengerAlgorithm = package$.MODULE$; // @see ScavengerAppJ                                                              
    protected Computation$ scavengerComputation = Computation$.MODULE$;
    public void setScavengerContext(Context scavengerContext)
    {
        this.scavengerContext = scavengerContext;
    }
    public double getDistance(Object value1, Object value2, DistanceMeasure distanceMeasure)
    {
        //System.out.println("getDistance()");
        double distance = 0.0;

        distanceMeasure.setValues(value1, value2);
        Computation<Double> computationData = scavengerComputation.apply("Values_"+value1 + "_" + value2, distance);//.cacheLocally().backUp();
        Algorithm<Double, Double> algorithm = scavengerAlgorithm.cheap("calculateDistance", distanceMeasure);
        Computation<Double> computation = algorithm.apply(computationData).cacheLocally();//cacheGlobally(); 
                                                                            // at the moment cacheGlobally() caches on the Master,
                                                                            // worker nodes do not have access to the master's cache.
                                                                            // So, currently (in worst case) every worker will calculate 
                                                                            // the distance between two values once.
        Future<Double> future = scavengerContext.submit(computation);
        
        try
        {
            distance = (Double)Await.result(future, (new Timeout(Duration.create(40, "seconds")).duration()));
        }
        catch(Exception e) 
        { 
            e.printStackTrace(); 
        }
        
        //System.out.println("getDistance() distance : " + distance);
        return distance;
    }*/
    
    
    ///////////////// Methods used for Bottom-Up clustering //////////////////////
    
    /**
     * 
     * @param treeNodeList Contains a list of the nodes that could be joined
     * @param numberOfClusters The number of clusters that should be created
     * @param startNumberOfTreeNodes The number of clusters started with
     *
     * @return A list of the closest nodes (size will be same as numberOfStartSplinterNodes)
     */
    public List<Integer[]> getJoinNodes(List<TreeNode<T>> treeNodeList, int numberOfClusters, int startNumberOfTreeNodes)
    {
        List<Integer[]> joinNodes = new ArrayList<Integer[]>();
        List<Double> smallestDistances = new ArrayList<Double>();
        if(treeNodeList.size() > numberOfClusters)
        {
            for(int i = 0; i < treeNodeList.size(); i++)
            {
                for(int j = 0; j < treeNodeList.size(); j++)
                {
                    if(i == j)
                    {
                        continue;
                    }
                    double distance = calculateDistanceBetweenTwoClusters(treeNodeList.get(i), treeNodeList.get(j));
                    
                    if(smallestDistances.size() < numberOfStartSplinterNodes)
                    {
                        Integer[] array = {i, j};
                        joinNodes.add(array);
                        smallestDistances.add(distance);
                    }
                    else
                    {
                        int index = getLargestValueIndex(smallestDistances);
                        if(-1 != index)
                        {
                            if (distance <  smallestDistances.get(index))
                            {
                                Integer[] array = {i, j};
                                joinNodes.set(index, array);
                                smallestDistances.set(index, distance);
                            }
                        }
                    }
                    
                }
            }
        }
        return joinNodes;
    }
    
    /**
     * The average distance between every DataItem in the clusters.
     * 
     */
    public double calculateDistanceBetweenTwoClusters(TreeNode<T> cluster1, TreeNode<T> cluster2)
    {
        double distance = 0;
        for(int i = 0; i < cluster1.getData().size(); i++)
        {
            for(int j = 0; j < cluster2.getData().size(); j++)
            {
                List<DataItem<T>> temp = new ArrayList<DataItem<T>>();
                temp.add(cluster1.getData().get(i));
                temp.add(cluster2.getData().get(j));                
                distance = distance + calculateAverage(temp, 1);
            }
        }
        return distance / (double)(cluster1.getData().size() * cluster2.getData().size());
    }
    
    /**
     *
     */
    private int getLargestValueIndex(List<Double> distances)
    {
        double max = 0.0;
        int index = 0;
        for (int i = 0; i < distances.size(); i++)
        {
            if (distances.get(i) > max)
            {
                max = distances.get(i);
                index = i;
            }
        }
        return index;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////
}

