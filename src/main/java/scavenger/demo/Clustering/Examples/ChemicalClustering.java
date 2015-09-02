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
 * Clustering of chemical data
 */
class ChemicalClustering implements java.io.Serializable
{   
    // Strings used in the properties file
    private final String ARFF_FILE = "ARFF_FILE";
    private final String EUCLIDEAN = "euclidean";
    private final String TANIMOTO = "tanimoto";
    private final String WEIGTH = "_weight"; // eg. tanimoto_1_weight
    
    private final String RUN_TIME = "RUN_TIME";
    private final String NUM_START_SPLITER_NODES = "NUM_START_SPLITER_NODES";
    private final String[] PROPERTY_NAMES = {ARFF_FILE, EUCLIDEAN, TANIMOTO, RUN_TIME, NUM_START_SPLITER_NODES};
   
    private List<DistanceMeasureSelection> dataInformationList = new ArrayList<DistanceMeasureSelection>();
    private List<DataItem<Object>> initialCluster = new ArrayList<DataItem<Object>>();
    
    public ChemcalData(String fileName)
    {   
        Properties properties = setup(fileName);
         
        TreeNode<Object> input = new TreeNode<Object>(initialCluster);
        Diana<Object> diana = new Diana<Object>(dataInformationList); 
        diana = setProperties(diana, properties);
        
        // Run the clustering        
        TreeNode<Object> node = diana.runClustering(input);        
        diana.endClustering();
        
        // Print the results
        System.out.println("Printing end result : ");
        diana.printTree(node);
    }
    
    /**
     * Gets the information from the given properties file, 
     * sets up the distance measures,
     * and reads in the data to be clustered
     * 
     * @param propertiesFilePath 
     */ 
    private Properties setup(String propertiesFilePath)
    {
        System.out.println("ChemcalData running setup");
        List<String> dataNames = new ArrayList<String>();
        Properties properties = new Properties();
        
        // 1. read properties file
        try
        {
            InputStream input = new FileInputStream(propertiesFilePath);
            properties.load(input);
            input.close();
        }
        catch(IOException ex) 
        {
            System.out.println("Failed to read : " + propertiesFilePath);
            ex.printStackTrace();
            System.exit(1);
        }
        
        // 2. Set-up the distance measures
        HashMap<String, List<String>> distanceAttributes = setupDistanceMeasures(properties);
        
        // 3. read in the data from the ARFF_FILE
        try
        {
            String arffFile = properties.getProperty(ARFF_FILE);
            Scanner scan = new Scanner(new File(arffFile));
            List<String> orderedAttributeList = getOrderedAttributeList(scan, properties);
            //System.out.println("orderedAttributeList : " + orderedAttributeList );
            
            String temp = scan.nextLine();
            while (!temp.contains("@data"))
            {
                temp = scan.nextLine();
            }
            
            while (scan.hasNextLine())
            {
                temp = scan.nextLine();
                final String[] splitString = temp.split(",");
                HashMap<String, Object> map = new HashMap<String, Object>(); // create a HashMap
                
                for(String key : distanceAttributes.keySet())
                {
                    if(key.contains(TANIMOTO))
                    {
                        List<String> subSetAttributes = getOrderedAttributes(orderedAttributeList, distanceAttributes.get(key));
                        BitSet bitSet = setupTanimotoData(splitString, subSetAttributes, orderedAttributeList);
                        map.put(key, bitSet);
                    }
                    else if(key.contains(EUCLIDEAN))
                    {
                        List<String> subSetAttributes = getOrderedAttributes(orderedAttributeList, distanceAttributes.get(key));
                        List<Double> euclideanValues = setupEuclideanData(splitString, subSetAttributes, orderedAttributeList);
                        map.put(key, euclideanValues);
                    }
                }
                DataItem<Object> item = new DataItem<Object>(Integer.toString(initialCluster.size()), map); // to give a unique id to the DataItem the current size of the initialCluster is used. (Is optional, and doesn't have to be unique. Only used by this.printTree())
                initialCluster.add(item);                    
            }
            scan.close();
        }
        catch(IOException ex) 
        {
            System.out.println("Failed to read arff file ");
            ex.printStackTrace();
            System.exit(1);
        }
        return properties;
    } 
    
    
    
    /**
     *
     * @param properties
     * @return HashMap containing key = distance metric identifier, value = list of attributes. (eg. key = tanimoto_1, value = [F1_Ring_1382529864, F2_AromaticRing_547664213, F3_PhenolicRing_448747194])
     */
    private HashMap<String, List<String>> setupDistanceMeasures(Properties properties)
    {    
        HashMap<String, List<String>> distanceAttributes = new HashMap<String, List<String>>();
        for(String attribute : properties.stringPropertyNames())
        {
            if (isProperty(attribute))
            {
                continue;
            }
            
            String distanceMeasureId = properties.getProperty(attribute);
            if (distanceAttributes.containsKey(distanceMeasureId)) // distance measure has already been created for this attribute
            {   
                distanceAttributes.get(distanceMeasureId).add(attribute);
            }
            else
            {                
                // make distance measure
                DistanceMeasure distanceMeasure = null;                
                if(distanceMeasureId.contains(TANIMOTO))
                {
                    distanceMeasure = new Tanimoto();
                }
                else if(distanceMeasureId.contains(EUCLIDEAN))
                {
                    distanceMeasure = new EuclideanDistance(Double.parseDouble(properties.getProperty(distanceMeasureId)));
                }
                
                // add weighting
                double weight = 1;
                try
                {
                    weight = Double.parseDouble(properties.getProperty(distanceMeasureId + WEIGTH));
                }
                catch(java.lang.NullPointerException ex)
                {
                    System.out.println("Use default weight : 1");
                }
                
                DistanceMeasureSelection distanceSelection = new DistanceMeasureSelection(distanceMeasureId, distanceMeasure, weight);
                dataInformationList.add(distanceSelection);
    
                List<String> tempList = new ArrayList<String>();
                tempList.add(attribute);
                distanceAttributes.put(distanceMeasureId, tempList);
            }
        }
        return distanceAttributes;
    }
    
    /**
     * Gets the data for a single record for a tanimoto distance measure
     *
     * @param data 
     * @param attributes The attributes who's distance will be calculated using eucldiean distance.
     * @param orderedAttributeList The full list of attributes. Used to find the data index that the attributes are at.
     */
    private BitSet setupTanimotoData(final String[] data, List<String> attributes, List<String> orderedAttributeList)
    {
        BitSet bitSet = new BitSet(attributes.size());
        for (int i = 0; i < attributes.size(); i++)
        {
            String attribute = attributes.get(i);
            
            if (data[orderedAttributeList.indexOf(attribute)].equals("1")) 
            {
                bitSet.set(i);
            }
        }
        return bitSet;
    }
    
    /**
     * Gets the data for a single record for a euclidean distance measure
     *
     * @param data 
     * @param attributes The attributes who's distance will be calculated using eucldiean distance.
     * @param orderedAttributeList The full list of attributes. Used to find the data index that the attributes are at.
     */
    private List<Double> setupEuclideanData(final String[] data, List<String> attributes, List<String> orderedAttributeList)
    {
        List<Double> euclideanValues = new ArrayList<Double>();
        for (int i = 0; i < attributes.size(); i++)//String value : values)
        {
            String attribute = attributes.get(i);
            euclideanValues.add(Double.parseDouble(data[orderedAttributeList.indexOf(attribute)]));
        }
        return euclideanValues;
    }
    
    
    /**
     * Orders the unorderedAttributes based on the order of orderedAttributeList.
     *
     * @param orderedAttributeList full ordered list of attributes
     * @param unorderedAttributes An unordered subset of the attributes
     * @return ordered subset of the attributes
     */
    private List<String> getOrderedAttributes(List<String> orderedAttributeList, List<String> unorderedAttributes)
    {
        List<String> values = new ArrayList<String>();
        for(String attribute : orderedAttributeList)
        {
            if (unorderedAttributes.contains(attribute))
            {
                values.add(attribute);
            }
        }
        return values;
    }
    
    /**
     * 
     * @param scan The arff file being read
     * @param properties Contains the attributes the clustering will be run on
     *
     * @return The list of attributes. Same order as arff file.
     */
    private List<String> getOrderedAttributeList(Scanner scan, Properties properties)
    {
        String line = scan.nextLine();
        while (!line.contains("@attribute"))
        {
            line = scan.nextLine();
        }
        List<String> orderedAttributeList = new ArrayList<String>();
        while(line.contains("@attribute"))
        {
            boolean added = false;
            for(String dataName : properties.stringPropertyNames())
            {
                if (line.contains(dataName))
                {
                    orderedAttributeList.add(dataName);
                    added = true;
                    break;
                }                    
            }
            if (!added) // not an attribute used to cluster, so leave blank gap
            {
                orderedAttributeList.add("");
            }
            line = scan.nextLine();
        }
        return orderedAttributeList;
    }
    
    /** 
     * 
     * List.contains() performs String.equals(), but we want String.contains()
     */
    private boolean isProperty(String attribute)
    {
        for(String property : PROPERTY_NAMES)
        {
            if (attribute.contains(property))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     *
     */
    private Diana setProperties(Diana diana, Properties properties)
    {
        if(properties.getProperty(RUN_TIME) != null)
        {
            diana.setRunTimeSeconds(Integer.parseInt(properties.getProperty(RUN_TIME)));
        }
        
        if(properties.getProperty(NUM_START_SPLITER_NODES) != null)
        {
            diana.setNumberOfStartSplinterNodes(Integer.parseInt(properties.getProperty(NUM_START_SPLITER_NODES)));
        }
        return diana;
    }    

    
    private void calculateGoodness(TreeNode<Object> node)
    {
        //TODO
    }
    
    public static void main(final String[] args)
    {
         String fileName = "/Users/helen/Documents/MainzUni/scavengerClean/scavenger/src/main/java/scavenger/demo/Clustering/clustering.properties";//args[1];
        ChemicalClustering chemicalClustering = new ChemicalClustering(fileName);
    }
    
}