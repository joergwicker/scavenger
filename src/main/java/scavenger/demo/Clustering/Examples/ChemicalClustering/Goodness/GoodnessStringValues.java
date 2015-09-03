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

/**
 * 
 */
class GoodnessStringValues extends Goodness
{   
    private List<String> goodnessAttributeValues = new ArrayList<String>();
    private List<String> goodnessAttributePossibleValues;
    
    /**
     * parses the GOODNESS_ATTRIBUTE_VALUES
     */
    public GoodnessStringValues(Properties properties, final String GOODNESS_ATTRIBUTE_VALUES)
    {
        String line = properties.getProperty(GOODNESS_ATTRIBUTE_VALUES);
        goodnessAttributePossibleValues = Arrays.asList(line.substring(line.indexOf("{") + 1, line.indexOf("}")).split(","));
    }
    
    
    public void addGoodnessAttributeValue(String value)
    {
        goodnessAttributeValues.add(value);
    }
    
    
    /**
     *
     */
    public int[] printNumberInEachSet(List<DataItem> cluster)
    {
        int[] numInSets = new int[goodnessAttributePossibleValues.size()]; 
        for (int i = 0; i < cluster.size(); i++)
        {
            String attribute = goodnessAttributeValues.get(Integer.parseInt(cluster.get(i).getId()));
            int index = goodnessAttributePossibleValues.indexOf(attribute);
            numInSets[index] = numInSets[index]+1;
        }
        for(int i = 0; i < numInSets.length; i++)
        {
            System.out.println("    " + goodnessAttributePossibleValues.get(i) + " : " + numInSets[i]);
        }
        return numInSets;
    }
    
    
    /**
     *
     */
    public void calculateGoodness(TreeNode<Object> node)
    {
        OrdinalStringDistance stringDistance = new OrdinalStringDistance(goodnessAttributePossibleValues);
        DianaDistanceFunctions distanceFunctions = new DianaDistanceFunctions();
        List<TreeNode> leaves = distanceFunctions.findLeafNodes(node.getRoot());
        int[][] numInSetsForLeaves = new int[leaves.size()][goodnessAttributePossibleValues.size()];
        
        double totalDistance = 0;
        for (int i = 0; i < leaves.size(); i++)
        {
            System.out.print("Cluster " + i);
            List<DataItem> cluster = leaves.get(i).getData();
            
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
                    total = total + stringDistance.getDistance(goodnessAttributeValues.get(Integer.parseInt(cluster.get(j).getId())).trim(), goodnessAttributeValues.get(Integer.parseInt(cluster.get(k).getId())).trim());           
                }
                clusterDistance = clusterDistance + (total / cluster.size());
            } 
            System.out.println(" : ( calculateGoodness " + " = " + (1 - (clusterDistance / cluster.size())) + " )");
            numInSetsForLeaves[i] = printNumberInEachSet(cluster);
            
            totalDistance = totalDistance + (clusterDistance / cluster.size());
        }
        totalDistance = totalDistance / leaves.size();
        totalDistance = (1-totalDistance);
        System.out.println("calculateGoodness total = " + totalDistance);
        testClusters(numInSetsForLeaves);
    }
    
    public void testClusters(int[][] numInSetsForLeaves)
    {
        int numberIncorrectlyClustered = 0;
        int numberClustered = 0;
        for(int j = 0; j < numInSetsForLeaves[0].length; j++)
        {
            int maxNum = -1;
            int maxIndex = -1;
            for(int i = 0; i < numInSetsForLeaves.length; i++)
            {
                if(numInSetsForLeaves[i][j] > maxNum)
                {
                    maxNum = numInSetsForLeaves[i][j];
                    if(maxNum == maxValueInArray(numInSetsForLeaves[i]))
                    {
                        if (maxIndex != -1)
                        {
                            numberIncorrectlyClustered = numberIncorrectlyClustered + numInSetsForLeaves[maxIndex][j];
                        }
                        maxIndex = i;
                    }
                    else
                    {
                        numberIncorrectlyClustered = numberIncorrectlyClustered + numInSetsForLeaves[i][j];
                        maxIndex = -1;
                    }
                }
                else
                {
                    numberIncorrectlyClustered = numberIncorrectlyClustered + numInSetsForLeaves[i][j];
                }
                numberClustered = numberClustered + numInSetsForLeaves[i][j];
            }
        }
        System.out.println("numberIncorrectlyClustered : " + numberIncorrectlyClustered);
        System.out.println("numberClustered : " + numberClustered);
        System.out.println("percentage : " + ((double)(numberClustered-numberIncorrectlyClustered)/(double)numberClustered)*100.00);
    }
    
    
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