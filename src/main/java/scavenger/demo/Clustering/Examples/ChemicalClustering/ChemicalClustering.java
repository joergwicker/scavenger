package scavenger.demo.clustering.examples;

import scavenger.demo.clustering.distance.*;
import scavenger.demo.clustering.*;
import scavenger.demo.clustering.errorCalculation.*;
import scavenger.demo.clustering.resultHandler.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Date;
import java.util.Scanner;
import java.util.BitSet;
import java.util.Properties;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Clustering of chemical data
 */
class ChemicalClustering implements java.io.Serializable
{   
    // Strings used in the properties file
    protected final String ARFF_FILE = "ARFF_FILE";
    protected final String EUCLIDEAN = "euclidean";
    protected final String TANIMOTO = "tanimoto";
    protected final String WEIGTH = "_weight"; // eg. tanimoto_1_weight
    
    protected final String RUN_TIME = "RUN_TIME";
    protected final String START_SPLINTER_NODES = "START_SPLINTER_NODES";
    
    protected final String TEST_ATTRIBUTE = "TEST_ATTRIBUTE";
    protected final String TEST_ATTRIBUTE_VALUES = "TEST_ATTRIBUTE_VALUES"; 
    
    protected final String SPLINTER_NUMBER = "CLUSTERS";
    protected final String OUTPUT_FILE = "OUTPUT_FILE";
    
    protected final String TRIMMED_MEAN_PERCENT = "TRIMMED_MEAN_PERCENT";
    
    protected final String ERROR_THRESHOLD = "ERROR_THRESHOLD"; // when clusters are all below this threshold the clustering is finished
    
    protected final String ERROR_CALCULATION = "ERROR_CALCULATION";
    
    protected final String[] PROPERTY_NAMES = {ARFF_FILE, EUCLIDEAN, TANIMOTO, RUN_TIME, START_SPLINTER_NODES, TEST_ATTRIBUTE, TEST_ATTRIBUTE_VALUES, SPLINTER_NUMBER, OUTPUT_FILE, TRIMMED_MEAN_PERCENT, ERROR_THRESHOLD, ERROR_CALCULATION};
   
    protected List<DistanceMeasureSelection> dataInformationList = new ArrayList<DistanceMeasureSelection>();
    protected List<DataItem<Object>> initialCluster = new ArrayList<DataItem<Object>>();
    
    protected ResultHandler resultHandle = null;
    
    public ChemicalClustering()
    {   
        
    }
    public void runChemicalClustering(String fileName)
    {
        // read in the data
        Properties properties = setup(fileName);         
        TreeNode<Object> input = new TreeNode<Object>(initialCluster);
        Diana<Object> diana = new Diana<Object>(dataInformationList); 
        diana = setProperties(diana, properties);
        diana.setResultHandler(resultHandle);
        
        // Run the clustering 
        
        Date start = new Date();
        
        TreeNode<Object> node = diana.runClustering(input);        
        diana.endClustering();
        
        Date end = new Date();
        System.out.println("Clustering took : " + (end.getTime() - start.getTime()) + " milliseconds");
        
        // Print the results
        System.out.println("Printing end result : ");
        //diana.printTree(node);
        
        // Check results
        if (resultHandle != null)
        { 
            resultHandle.handleResults(node);
        }
    
    }
    
    /**
     * Gets the information from the given properties file, 
     * sets up the distance measures,
     * and reads in the data to be clustered
     * 
     * @param propertiesFilePath 
     */ 
    protected Properties setup(String propertiesFilePath)
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
        
        // set-up for the Goodness calculatation
        if (properties.getProperty(TEST_ATTRIBUTE) != null)
        {
            resultHandle = new ResultHandlerStringValues<Object>(properties, TEST_ATTRIBUTE_VALUES, SPLINTER_NUMBER, OUTPUT_FILE);
        }
        
        // 3. read in the data from the ARFF_FILE
        try
        {
            String arffFile = properties.getProperty(ARFF_FILE);
            Scanner scan = new Scanner(new File(arffFile));
            List<String> orderedAttributeList = getOrderedAttributeList(scan, properties);
            
            String line = scan.nextLine();
            while (!line.contains("@data"))
            {
                line = scan.nextLine();
            }
            
            while (scan.hasNextLine())
            {
                line = scan.nextLine();
                final String[] splitString = line.split(",");
                HashMap<String, Object> map = new HashMap<String, Object>(); // create a HashMap
                
                // for each of the different distance measures, 
                //   get the data the measure will be used on
                for(String key : distanceAttributes.keySet()) 
                {
                    if(key.toLowerCase().contains(TANIMOTO))
                    {
                        List<String> subSetAttributes = getOrderedAttributes(orderedAttributeList, distanceAttributes.get(key));
                        BitSet bitSet = setupTanimotoData(splitString, subSetAttributes, orderedAttributeList);
                        map.put(key, bitSet);
                    }
                    else if(key.toLowerCase().contains(EUCLIDEAN))
                    {
                        List<String> subSetAttributes = getOrderedAttributes(orderedAttributeList, distanceAttributes.get(key));
                        List<Double> euclideanValues = setupEuclideanData(splitString, subSetAttributes, orderedAttributeList);
                        map.put(key, euclideanValues);
                    }
                }
                
                if (resultHandle != null)
                {
                    resultHandle.addAttributeValue(splitString[orderedAttributeList.indexOf(properties.getProperty(TEST_ATTRIBUTE))]);
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
     * Sets up the dataInformationList with the different distance measures (DistanceMeasureSelection).
     *  Each DistanceMeasureSelection will contain the distance measure being used, the weight, and a list of attributes(ids) which the distance measure will be used on.
     *
     * @param properties
     * @return HashMap containing key = distance metric identifier, value = list of attributes. (eg. key = tanimoto_1, value = [F1_Ring_1382529864, F2_AromaticRing_547664213, F3_PhenolicRing_448747194])
     */
    protected HashMap<String, List<String>> setupDistanceMeasures(Properties properties)
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
                    try
                    {
                        distanceMeasure = new EuclideanDistance(Double.parseDouble(properties.getProperty(distanceMeasureId)));
                    }
                    catch(java.lang.NullPointerException ex)
                    {
                        System.out.println("Use default parameter (1) for " + distanceMeasureId);
                        distanceMeasure = new EuclideanDistance(1);
                    }
                }
                
                // add weighting
                double weight = 1;
                try
                {
                    weight = Double.parseDouble(properties.getProperty(distanceMeasureId + WEIGTH));
                }
                catch(java.lang.NullPointerException ex)
                {
                    System.out.println("Use default weight (1) for " + distanceMeasureId);
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
    protected BitSet setupTanimotoData(final String[] data, List<String> attributes, List<String> orderedAttributeList)
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
    protected List<Double> setupEuclideanData(final String[] data, List<String> attributes, List<String> orderedAttributeList)
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
    protected List<String> getOrderedAttributes(List<String> orderedAttributeList, List<String> unorderedAttributes)
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
    protected List<String> getOrderedAttributeList(Scanner scan, Properties properties)
    {
        String line = scan.nextLine();
        while (!line.contains("@attribute"))
        {
            line = scan.nextLine();
        }
        List<String> orderedAttributeList = new ArrayList<String>();
        while(line.contains("@attribute"))
        {
            if (properties.getProperty(TEST_ATTRIBUTE) != null)
            {
                if(line.contains(properties.getProperty(TEST_ATTRIBUTE)))
                {
                    orderedAttributeList.add(properties.getProperty(TEST_ATTRIBUTE));
                    line = scan.nextLine();
                    continue;
                }
            }
            boolean added = false;
            for(String attributeName : properties.stringPropertyNames())
            {
                if(isProperty(attributeName))
                {
                    continue;
                }
                if (line.contains(attributeName))
                {
                    orderedAttributeList.add(attributeName);
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
    protected boolean isProperty(String attribute)
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
     * Setup Diana with the properties supplied by the user
     *
     * @param diana
     * @param properties
     *
     * @param Diana with the properties set
     */
    protected Diana setProperties(Diana diana, Properties properties)
    {
        // RUN_TIME
        if(properties.getProperty(RUN_TIME) != null)
        {
            diana.setRunTimeSeconds(Integer.parseInt(properties.getProperty(RUN_TIME)));
        }
        
        // START_SPLINTER_NODES
        if(properties.getProperty(START_SPLINTER_NODES) != null)
        {
            diana.setNumberOfStartSplinterNodes(Integer.parseInt(properties.getProperty(START_SPLINTER_NODES)));
        }
        
        // SPLINTER_NUMBER
        if (properties.getProperty(SPLINTER_NUMBER) != null)
        {
            diana.setNumberOfClusters(Integer.parseInt(properties.getProperty(SPLINTER_NUMBER)));
        }
        
        // TRIMMED_MEAN_PERCENT - might be single or list of integer values
        if (properties.getProperty(TRIMMED_MEAN_PERCENT) != null)
        {
            String[] strs = properties.getProperty(TRIMMED_MEAN_PERCENT).split("[, ]");
            List<Integer> values = new ArrayList<Integer>();
            for(String str : strs)
            {
                if (!str.equals(""))
                {
                    values.add(Integer.parseInt(str));
                }
            }
            diana.setTrimmedMeanPercent(values);
        }
        
        // ERROR_THRESHOLD
        if (properties.getProperty(ERROR_THRESHOLD) != null)
        {
            diana.setErrorThreshold(Double.parseDouble(properties.getProperty(ERROR_THRESHOLD)));
        }
        
        // ERROR_CALCULATION
        if (properties.getProperty(ERROR_CALCULATION) != null)
        {
            if(properties.getProperty(ERROR_CALCULATION).equals("PurityErrorCalculation"))
            {
                if (properties.getProperty(ERROR_THRESHOLD) != null)
                {
                    diana.setErrorCalculation(new PurityErrorCalculation(Double.parseDouble(properties.getProperty(ERROR_THRESHOLD)), (ResultHandlerStringValues)resultHandle));
                }
                else
                {
                    diana.setErrorCalculation(new PurityErrorCalculation(0.0, (ResultHandlerStringValues)resultHandle));
                }
            }
        }
        return diana;
    }    
    
    public static void main(final String[] args)
    {
        String fileName = args[0];
        ChemicalClustering chemicalClustering = new ChemicalClustering();
        chemicalClustering.runChemicalClustering(fileName);
    }
    
}