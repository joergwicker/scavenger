package scavenger.demo.clustering.examples;

import scavenger.demo.clustering.distance.*;
import scavenger.demo.clustering.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.net.URL;
import java.util.Scanner;
import java.io.IOException;
import java.util.BitSet;
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
 * 
 */
class ResultHandlerStringValues extends ResultHandler<Object>
{   
    private List<String> attributeValues = new ArrayList<String>();
    private List<String> attributePossibleValues;
    
    private int numberOfClusters = -1;
    private String outputFile = "";
    private String outputStr = "";    
    private DecimalFormat df = new DecimalFormat("##0.0000");
    
   
    /**
     *
     */
    public ResultHandlerStringValues(Properties properties, final String TEST_ATTRIBUTE_VALUES, final String SPLINTER_NUMBER, final String OUTPUT_FILE)
    {
        String line = properties.getProperty(TEST_ATTRIBUTE_VALUES);
        attributePossibleValues = Arrays.asList(line.substring(line.indexOf("{") + 1, line.indexOf("}")).split(","));
        
        if (properties.getProperty(SPLINTER_NUMBER) != null)
        {
            this.numberOfClusters = Integer.parseInt(properties.getProperty(SPLINTER_NUMBER));
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
    
    
    /**
     *
     * @param cluster The DataItems from a cluster
     * @return The number of DataItems belonging to each set (attributePossibleValues)
     */
    public int[] getNumberInEachSet(List<DataItem<Object>> cluster)
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
            //System.out.println("    " + goodnessAttributePossibleValues.get(i) + " : " + numInSets[i]);
        }
        return numInSets;
    }
    
    
    /**
     * Diana clustering
     */
    public void handleResults(TreeNode<Object> node)
    {
        outputStr = node.printTree();
    
        
        List<TreeNode<Object>> leaves = new ArrayList<TreeNode<Object>>();
       
        if (numberOfClusters == -1)
        {
            leaves = node.getRoot().findLeafNodes();
        }
        else
        {
            leaves = node.getRoot().findLeafNodes(numberOfClusters);
        }
        handleResults(leaves);
    }
    
    /**
     * BottomUp clustering
     */
    public void handleResults(TreeNodeList<Object> node)
    {
        outputStr = node.print();
        handleResults(node.getTreeNodeData());
    }  
    
    /**
     *
     */
    public void handleResults(List<TreeNode<Object>> leaves)
    {
        OrdinalStringDistance stringDistance = new OrdinalStringDistance(attributePossibleValues);
        DianaDistanceFunctions distanceFunctions = new DianaDistanceFunctions();
        int[][] numInSetsForLeaves = new int[leaves.size()][attributePossibleValues.size()];
        
        double totalDistance = 0;
        for (int i = 0; i < leaves.size(); i++)
        {
            outputStr = outputStr + "Cluster " + i;
            //System.out.print("Cluster " + i);
            List<DataItem<Object>> cluster = leaves.get(i).getData();
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
            //System.out.println(" : ( calculateGoodness " + " = " + (1 - (clusterDistance / cluster.size())) + " )");
            
            numInSetsForLeaves[i] = getNumberInEachSet(cluster);
            
            totalDistance = totalDistance + (clusterDistance / cluster.size());
        }
        totalDistance = totalDistance / leaves.size();
        totalDistance = (1-totalDistance);
        //System.out.println("calculateGoodness total = " + totalDistance);
        outputStr = outputStr + "average distance = " + totalDistance + "\n\n";
        
        testClusters(numInSetsForLeaves);
        System.out.println(outputStr);
        writeToFile();
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
    
    
    /**
     * 
     *
     * @param numInSetsForLeaves [leafIndex][SetIndex]
     *
     */
    private double testClusters(int[][] numInSetsForLeaves)
    {
        int[] givenSets = new int[attributePossibleValues.size()]; 
        int numberIncorrectlyClustered = 0;
        int numberClustered = 0;
        
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
                if(containsIndex != -1) // stops a cluster being assigned to multiple sets
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
        
        //System.out.println("numberIncorrectlyClustered : " + numberIncorrectlyClustered);
        outputStr = outputStr + "numberIncorrectlyClustered : " + numberIncorrectlyClustered + " (" + df.format(((double)(numberIncorrectlyClustered)/(double)numberClustered)*100.00) + "%)\n";
        //System.out.println("numberClustered : " + numberClustered);
        outputStr = outputStr + "numberClustered : " + numberClustered + "\n";
        
        return ((double)(numberIncorrectlyClustered))/((double)numberClustered);
    }
    
    /**
     *
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
     *
     */
    private int maxValueInArray(int[] array)
    {
        int max = 0;
        for (int i = 0; i < array.length; i++)
        {
            if (array[i] > max)
            {
                max = array[i];
            }
        }
        return max;
    }
    
    
}