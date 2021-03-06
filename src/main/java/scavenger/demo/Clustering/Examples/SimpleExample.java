package scavenger.demo.clustering.examples;

import scavenger.demo.clustering.errorCalculation.*;
import scavenger.demo.clustering.distance.*;
import scavenger.demo.clustering.enums.*;
import scavenger.demo.clustering.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashMap;

import java.net.URL;
import java.util.Scanner;
import java.io.IOException;

/**
 * A basic example of using Euclidean distance, when performing Diana clustering.
 *
 */
class SimpleExample implements java.io.Serializable
{   
    public SimpleExample()
    {
        // 1. Create distance measure
        DistanceMeasure<List<Double>> distanceMeasure = new EuclideanDistance(9.0);
        
        // 2. Load data
        List list1 = new ArrayList<Double>() 
            {{
                add(0.0);
                add(0.0);
                add(0.0);
            }};
                
        List list2 = new ArrayList<Double>() 
            {{
                add(9.0);
                add(9.0);
                add(9.0);
            }};
            
        List list3 = new ArrayList<Double>() 
            {{
                add(2.0);
                add(1.0);
                add(1.0);
            }};
        List list4 = new ArrayList<Double>() 
            {{
                add(3.0);
                add(2.0);
                add(1.0);
            }};
        List list5 = new ArrayList<Double>() 
            {{
                add(3.0);
                add(2.0);
                add(2.0);
            }};
        List list6 = new ArrayList<Double>() 
            {{
                add(1.0);
                add(1.0);
                add(1.0);
            }};
        List list7 = new ArrayList<Double>() 
            {{
                add(1.0);
                add(2.0);
                add(1.0);
            }};
            
        DataItem<Integer> item1 = new DataItem<Integer>("1", list1);
        DataItem<Integer> item2 = new DataItem<Integer>("2", list2);
        DataItem<Integer> item3 = new DataItem<Integer>("3", list3);
        DataItem<Integer> item4 = new DataItem<Integer>("4", list4);
        DataItem<Integer> item5 = new DataItem<Integer>("5", list5);
        DataItem<Integer> item6 = new DataItem<Integer>("6", list6);
        
        // 3. Create root node
        List<DataItem<Integer>> data = new ArrayList<DataItem<Integer>>();
        data.add(item1);
        data.add(item2);
        data.add(item3);
        data.add(item4);
        data.add(item5);
        data.add(item6);
        
        // 4.1 Set the root and diana
        TreeNode<Integer> input = new TreeNode<Integer>(data);
        Diana<Integer> diana = new Diana<Integer>(distanceMeasure);
        
        // 4.2 change the defaults
        diana.setErrorCalculation(new SimpleErrorCalculation(0.04));
        diana.setRunTimeSeconds(30);
        diana.setNumberOfStartSplinterNodes(2);
        diana.setDiameterMeasure(DiameterMeasure.LARGEST_AVERAGE_DISTANCE);
        
        // 5. Perform the clustering
        TreeNode<Integer> node = diana.runClustering(input);
        diana.endClustering();
        
        // 5. Print results
        node.printTree();
    }
    
    public static void main(final String[] args)
    {
        SimpleExample s = new SimpleExample();
    }
    
}