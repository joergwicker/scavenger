package scavenger.demo.clustering.resultHandler;

import scavenger.demo.clustering.distance.*;
import scavenger.demo.clustering.*;
import scavenger.demo.clustering.bottomUp.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Date;

import java.text.DecimalFormat;

/**
 * Calculates the number of items that have been correctly clustered.
 * Prints information about the clusters to cmd and file.
 *
 */
public class ResultHandlerStringValues<T> extends ResultHandler<T, String>
{   
    private List<String> attributeValues = new ArrayList<String>();
    private List<String> attributePossibleValues;
    
    private int numberOfClusters = -1;
    private String outputFile = "";
    private String outputStr = "";    
    private DecimalFormat df = new DecimalFormat("##0.0000");
    
   
    /**
     * Sets the user defined properties if they have been set.
     *
     * @param properties 
     * @param TEST_ATTRIBUTE_VALUES
     * @param NUMBER_OF_CLUSTERS
     * @param OUTPUT_FILE
     */
    public ResultHandlerStringValues(Properties properties, final String TEST_ATTRIBUTE_VALUES, final String NUMBER_OF_CLUSTERS, final String OUTPUT_FILE)
    {
        String line = properties.getProperty(TEST_ATTRIBUTE_VALUES);
        attributePossibleValues = Arrays.asList(line.substring(line.indexOf("{") + 1, line.indexOf("}")).split(","));
        
        if (properties.getProperty(NUMBER_OF_CLUSTERS) != null)
        {
            this.numberOfClusters = Integer.parseInt(properties.getProperty(NUMBER_OF_CLUSTERS));
        }
        
        if (properties.getProperty(OUTPUT_FILE) != null)
        {
            this.outputFile = properties.getProperty(OUTPUT_FILE);
        }
    }
    
    /**
     *
     */
    public void addAttributeValue(String value)
    {
        attributeValues.add(value);
    }
    
    public List<String> getAttributePossibleValues()
    {
        return attributePossibleValues;
    }
    
    public List<String> getAttributeValues()
    {
        return attributeValues;
    }
    
    
    /**
     * Diana clustering
     * Finds the leaf nodes and passes them to handleResults(List<TreeNode<T>> leaves)
     */
    public void handleResults(TreeNode<T> node)
    {
        outputStr = node.printTree();
    
        
        List<TreeNode<T>> leaves = new ArrayList<TreeNode<T>>();
       
        if (numberOfClusters == -1)
        {
            leaves = node.getRoot().findLeafNodes();
        }
        else
        {
            leaves = node.getRoot().findLeafNodes(numberOfClusters);
        }
        handleResults(leaves);
        System.out.println(outputStr);
        writeToFile();
    }
    
    /**
     * BottomUp clustering
     * Finds the leaf nodes and passes them to handleResults(List<TreeNode<T>> leaves)
     */
    public void handleResults(TreeNodeList<T> node)
    {
        outputStr = node.print();
        handleResults(node.getTreeNodeData());
        System.out.println(outputStr);
        writeToFile();
    }  
    
    /**
     * Calculates the OrdinalStringDistance for each cluster,
     * and calcuates the purity of the clusters. (@see testClusters())
     * 
     * @return Percentage of incorrectly clustered items
     */
    public double handleResults(List<TreeNode<T>> leaves)
    {
        OrdinalStringDistance stringDistance = new OrdinalStringDistance(attributePossibleValues);
        DianaDistanceFunctions distanceFunctions = new DianaDistanceFunctions();
        int[][] numInSetsForLeaves = new int[leaves.size()][attributePossibleValues.size()];
        
        double totalDistance = 0;
        // calculate the diameter of each cluster using the OrdinalStringDistance
        for (int i = 0; i < leaves.size(); i++)
        {
            outputStr = outputStr + "Cluster " + i;
            List<DataItem<T>> cluster = leaves.get(i).getData();
            // cluster distance 
            double clusterDistance = 0;
            for (int j = 0; j < cluster.size(); j++)
            {
                // item average distance
                double total = 0;
                for(int k = 0; k < cluster.size(); k++)
                {
                    if (j == k) 
                    {
                        continue;
                    }
                    total = total + stringDistance.getDistance(attributeValues.get(Integer.parseInt(cluster.get(j).getId())).trim(), attributeValues.get(Integer.parseInt(cluster.get(k).getId())).trim());           
                }
                clusterDistance = clusterDistance + (total / cluster.size());
            } 
            outputStr = outputStr + " : ( cluster distance " + " = " + (1 - (clusterDistance / cluster.size())) + " )\n";
            totalDistance = totalDistance + (clusterDistance / cluster.size());
            
            
            
            numInSetsForLeaves[i] = getNumberInEachSet(cluster); // used by testClusters()
        }
        totalDistance = totalDistance / leaves.size();
        totalDistance = (1-totalDistance);
        outputStr = outputStr + "average distance = " + totalDistance + "\n\n";
        
        
        return testClusters(numInSetsForLeaves);
    }
    
     /**
     *
     * @param cluster The DataItems from a cluster
     * @return The number of DataItems belonging to each set (attributePossibleValues)
     */
    public int[] getNumberInEachSet(List<DataItem<T>> cluster)
    {
        int[] numInSets = new int[attributePossibleValues.size()]; 
        for (int i = 0; i < cluster.size(); i++)
        {
            String attribute = attributeValues.get(Integer.parseInt(cluster.get(i).getId()));
            int index = attributePossibleValues.indexOf(attribute);
            numInSets[index] = numInSets[index]+1;
        }
        for(int i = 0; i < numInSets.length; i++)
        {
            outputStr = outputStr + "    " + attributePossibleValues.get(i) + " : " + numInSets[i] + "\n";
        }
        return numInSets;
    }
    
    
    /**
     * Works out which set should be assigned to each cluster and 
     * finds the number of items correctly clustered.
     *
     * @param numInSetsForLeaves [leafIndex][SetIndex]
     * @return Percentage of incorrectly clustered items
     */
    private double testClusters(int[][] numInSetsForLeaves)
    {
        int[] givenSets = new int[attributePossibleValues.size()]; 
        int numberIncorrectlyClustered = 0;
        int numberClustered = 0;
                
        // assigns each cluster to a set 
        for(int k = 0; k < attributePossibleValues.size(); k++)
        {
            for(int j = 0; j < numInSetsForLeaves[0].length; j++)
            {
                if(givenSets[j] != 0)
                {
                    continue;
                }
                int maxNum = -1;
                int maxIndex = -1;
                
                // Find the cluster that has the highest number of items belonging to the set
                for(int i = 0; i < numInSetsForLeaves.length; i++)
                {
                    if(numInSetsForLeaves[i][j] > maxNum)
                    {
                        int containsIndex = getIndex(givenSets, i+1);
                        if(containsIndex != -1) // check cluster has not already been asigned to a set
                        {
                            if(numInSetsForLeaves[i][containsIndex] > numInSetsForLeaves[i][j])
                            {
                                continue;
                            }
                        }
                        maxNum = numInSetsForLeaves[i][j];
                        maxIndex = i;
                    }
                }
                
                int containsIndex = getIndex(givenSets, maxIndex+1);
                if((containsIndex != -1) && (maxIndex > -1)) // stops a cluster being assigned to multiple sets
                {
                    if(numInSetsForLeaves[maxIndex][containsIndex] > maxNum)
                    {
                        continue;
                    }
                    else
                    {
                        givenSets[containsIndex] = 0;
                    }
                }
                givenSets[j] = maxIndex + 1; // Default is 0 (not assigned), so cluster number 0 should be 1 
            }
            
            if(getIndex(givenSets, 0) == -1) // all clusters have been assigned a set
            {
                break;
            }
        }

        
        // add up the number of correctly clustered nodes
        int total = 0;
        for (int i = 0; i < givenSets.length; i++)
        {
            if (givenSets[i] != 0)
            {
                total = total + numInSetsForLeaves[givenSets[i]-1][i];
            }
            for(int j = 0; j < numInSetsForLeaves.length; j++)
            {
                numberClustered = numberClustered + numInSetsForLeaves[j][i];
            }
        }
        outputStr = outputStr + "number correctly clustered : " + total + " (" + df.format(((double)(total)/(double)numberClustered)*100.00) + "%)\n";
        numberIncorrectlyClustered = numberClustered - total;
        
        outputStr = outputStr + "numberIncorrectlyClustered : " + numberIncorrectlyClustered + " (" + df.format(((double)(numberIncorrectlyClustered)/(double)numberClustered)*100.00) + "%)\n";
        outputStr = outputStr + "numberClustered : " + numberClustered + "\n";
        
        return ((double)(numberIncorrectlyClustered))/((double)numberClustered);
    }
    
    /**
     * Gets the index value is located at.
     * 
     * @return -1 if the value is not in the array. Otherwise, the index the value first appears
     */
    private int getIndex(int[] array, int value)
    {
        for (int i = 0; i < array.length; i++)
        {
            if (value == array[i])
            {
                return i;
            }
        }
        return -1;
    }
    
    
    /**
     * Appends the results to the ouput file.
     * 
     */
    private void writeToFile()
    {
        if(!outputFile.equals(""))
        {
            try
            {
                outputStr = "Time : " + new Date() + "\n Results : \n" + outputStr;
                Path path = Paths.get(outputFile);
                if (!Files.exists(path))
                {
                    Files.write(path, outputStr.getBytes(), StandardOpenOption.CREATE);
                }
                else
                {
                    Files.write(path, outputStr.getBytes(), StandardOpenOption.APPEND);
                }
            }
            catch (IOException ex) 
            {
                ex.printStackTrace();
            }
        }
    }
}