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

/**
 * 
 */
class ResultHandlerStringValues extends ResultHandler<Object>
{   
    private List<String> attributeValues = new ArrayList<String>();
    private List<String> attributePossibleValues;
    
    private int splinterNumber = -1;
    private String outputFile = "";
    
    private String outputStr = "";
    
    /**
     * parses the GOODNESS_ATTRIBUTE_VALUES
     */
    public ResultHandlerStringValues(Properties properties, final String TEST_ATTRIBUTE_VALUES)
    {
        String line = properties.getProperty(TEST_ATTRIBUTE_VALUES);
        attributePossibleValues = Arrays.asList(line.substring(line.indexOf("{") + 1, line.indexOf("}")).split(","));
    }
    
    
    public ResultHandlerStringValues(Properties properties, final String TEST_ATTRIBUTE_VALUES, int splinterNumber)
    {
        String line = properties.getProperty(TEST_ATTRIBUTE_VALUES);
        attributePossibleValues = Arrays.asList(line.substring(line.indexOf("{") + 1, line.indexOf("}")).split(","));
        
        this.splinterNumber = splinterNumber;
    }
    
    public ResultHandlerStringValues(Properties properties, final String TEST_ATTRIBUTE_VALUES, final String SPLINTER_NUMBER, final String OUTPUT_FILE)
    {
        String line = properties.getProperty(TEST_ATTRIBUTE_VALUES);
        attributePossibleValues = Arrays.asList(line.substring(line.indexOf("{") + 1, line.indexOf("}")).split(","));
        
        if (properties.getProperty(SPLINTER_NUMBER) != null)
        {
            this.splinterNumber = Integer.parseInt(properties.getProperty(SPLINTER_NUMBER));
        }
        
        if (properties.getProperty(OUTPUT_FILE) != null)
        {
            this.outputFile = properties.getProperty(OUTPUT_FILE);
        }
    }
    
    
    public void addAttributeValue(String value)
    {
        attributeValues.add(value);
    }
    
    
    /**
     *
     */
    public int[] printNumberInEachSet(List<DataItem<Object>> cluster)
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
     *
     */
    public void handleResults(TreeNode<Object> node)
    {
        outputStr = node.printTree();
    
        
        List<TreeNode<Object>> leaves = new ArrayList<TreeNode<Object>>();
       
        if (splinterNumber == -1)
        {
            leaves = node.getRoot().findLeafNodes();
        }
        else
        {
            leaves = node.getRoot().findLeafNodes(splinterNumber);
        }
        handleResults(leaves);
    }
    
    public void handleResults(TreeNodeList<Object> node)
    {
        System.out.println("handleResults TreeNodeList");
        outputStr = node.print();
        handleResults(node.getTreeNodeData());
    }  
    
    public void handleResults(List<TreeNode<Object>> leaves)
    {
        System.out.println("handleResults leaves");
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
            
            numInSetsForLeaves[i] = printNumberInEachSet(cluster);
            
            totalDistance = totalDistance + (clusterDistance / cluster.size());
        }
        totalDistance = totalDistance / leaves.size();
        totalDistance = (1-totalDistance);
        //System.out.println("calculateGoodness total = " + totalDistance);
        outputStr = outputStr + "average distance = " + totalDistance + "\n\n\n\n";
        
        testClusters(numInSetsForLeaves);
        System.out.println(outputStr);
        writeToFile();
    }
    
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
        //System.out.println("numberIncorrectlyClustered : " + numberIncorrectlyClustered);
        outputStr = outputStr + "numberIncorrectlyClustered : " + numberIncorrectlyClustered + "\n";
        //System.out.println("numberClustered : " + numberClustered);
        outputStr = outputStr + "numberClustered : " + numberClustered + "\n";
        //System.out.println("percentage : " + ((double)(numberClustered-numberIncorrectlyClustered)/(double)numberClustered)*100.00);
        outputStr = outputStr + "percentage : " + ((double)(numberClustered-numberIncorrectlyClustered)/(double)numberClustered)*100.00 + "\n";
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