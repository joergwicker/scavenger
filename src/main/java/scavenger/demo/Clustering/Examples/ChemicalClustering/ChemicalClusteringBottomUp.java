package scavenger.demo.clustering.examples;

import scavenger.demo.clustering.distance.*;
import scavenger.demo.clustering.*;
import scavenger.demo.clustering.enums.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Date;
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
 * Clustering of chemical data
 */
class ChemicalClusteringBottomUp extends ChemicalClustering implements java.io.Serializable
{   
    
    public void runChemicalClusteringBottomUp(String fileName)
    {
        // read in the data
        Properties properties = setup(fileName);         
        //TreeNode<Object> input = new TreeNode<Object>(initialCluster);
        List<TreeNode<Object>> inputToTreeNodeList = new ArrayList<TreeNode<Object>>();
        for(DataItem<Object> item : initialCluster)
        {
           List<DataItem<Object>> initialClustersItem = new ArrayList<DataItem<Object>>(); 
           initialClustersItem.add(item);
           inputToTreeNodeList.add(new TreeNode<Object>(initialClustersItem));
        }
        TreeNodeList<Object> input = new TreeNodeList<Object>(inputToTreeNodeList);
        
        BottomUp<Object> diana = new BottomUp<Object>(dataInformationList); 
        diana = (BottomUp<Object>)setProperties(diana, properties);
        //diana.setDiameterMeasure(DiameterMeasure.LARGEST_AVERAGE_DISTANCE);
        diana.setResultHandler(resultHandle);
        
        // Run the clustering  
        Date start = new Date();        
                    
        TreeNodeList<Object> node = diana.runClustering(input);        
        diana.endClustering();
        
        Date end = new Date();
        System.out.println("Clustering took : " + (end.getTime() - start.getTime()) + " milliseconds");
        
        // Check and print the results
        System.out.println("Printing end result : ");
        if (resultHandle != null)
        { 
            resultHandle.handleResults(node);
        }
    }
    
    public static void main(final String[] args)
    {
        //String fileName = "/Users/helen/Documents/MainzUni/scavengerClean/scavenger/src/main/java/scavenger/demo/Clustering/clustering.properties";//args[1];
        //String fileName = "/Users/helen/Documents/MainzUni/scavengerClean/scavenger/src/main/java/scavenger/demo/Clustering/clusteringEuclidean.properties";
        String fileName = "/Users/helen/Documents/MainzUni/scavengerClean/scavenger/src/main/java/scavenger/demo/Clustering/clusteringEuclideanAndTanimoto.properties";
        ChemicalClusteringBottomUp chemicalClustering = new ChemicalClusteringBottomUp();
        chemicalClustering.runChemicalClusteringBottomUp(fileName);
    }
}