package scavenger.demo.clustering.examples;

import scavenger.demo.clustering.distance.*;
import scavenger.demo.clustering.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.net.URL;
import java.util.Scanner;
import java.io.IOException;

/**
 * Example use of Diana 
 * 
 * 
 * https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.names
 */
class IrisExample implements java.io.Serializable
{   
    public IrisExample()
    {        
        // 1. Create the different distance measures with specific weights
        List<DistanceMeasureSelection> dataInformationList = new ArrayList<DistanceMeasureSelection>();
        
        DistanceMeasure<List<Double>> distanceMeasure1 = new EuclideanDistance(3.6); // 3.6 is the max distance between sepal lengths
        DistanceMeasureSelection distanceSelection1 = new DistanceMeasureSelection("sepal_length", distanceMeasure1, 1); // 1 is the weighting
        dataInformationList.add(distanceSelection1);
        
        DistanceMeasure<List<Double>> distanceMeasure2 = new EuclideanDistance(2.4);
        DistanceMeasureSelection distanceSelection2 = new DistanceMeasureSelection("sepal_width", distanceMeasure2, 1);
        dataInformationList.add(distanceSelection2);
        
        DistanceMeasure<List<Double>> distanceMeasure3 = new EuclideanDistance(5.9);
        DistanceMeasureSelection distanceSelection3 = new DistanceMeasureSelection("petal_length", distanceMeasure3, 1);
        dataInformationList.add(distanceSelection3);
        
        DistanceMeasure<List<Double>> distanceMeasure4 = new EuclideanDistance(2.4);
        DistanceMeasureSelection distanceSelection4 = new DistanceMeasureSelection("petal_width", distanceMeasure4, 1);
        dataInformationList.add(distanceSelection4);
        
        // 2. Load the data into a HashMap (@see DataItem)
        URL url;
        Scanner scan;
        List<DataItem<List<Double>>> initialCluster = new ArrayList<DataItem<List<Double>>>(); // EuclideanDistance takes List<Double>, so DataItem must a take List<Double>
        try
        {
            url = new URL("https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data");
            scan = new Scanner(url.openStream());
            
            while (scan.hasNext())
            {
                String temp = scan.next();
                final String[] splitString = temp.split(",");
                HashMap<String, List<Double>> map = new HashMap<String, List<Double>>(); // create a HashMap
                
                List<Double> list1 = new ArrayList<Double>()
                {{ 
                    add(Double.parseDouble(splitString[0]));
                }};                
                map.put("sepal_length", list1); // distanceSelection1 will be used; as same id/key given.
                
                List<Double> list2 = new ArrayList<Double>()
                {{ 
                    add(Double.parseDouble(splitString[1]));
                }};
                map.put("sepal_width", list2);
                
                List<Double> list3 = new ArrayList<Double>()
                {{ 
                    add(Double.parseDouble(splitString[2]));
                }};
                map.put("petal_length", list3);
                
                List<Double> list4 = new ArrayList<Double>()
                {{ 
                    add(Double.parseDouble(splitString[3]));
                }};
                map.put("petal_width", list4);
                
                DataItem<List<Double>> item = new DataItem<List<Double>>(Integer.toString(initialCluster.size()), map); // to give a unique id to the DataItem the current size of the initialCluster is used. (Is optional, and doesn't have to be unique. Only used by this.printTree())
                initialCluster.add(item);
            }
        }
        catch(IOException ex) 
        {
            ex.printStackTrace();
            System.exit(1);
        }
        
        // 3. Create the root node
        TreeNode<List<Double>> input = new TreeNode<List<Double>>(initialCluster);
        
        // 4. Run the clustering
        Diana<List<Double>> diana = new Diana<List<Double>>(dataInformationList); 
        TreeNode<List<Double>> node = diana.runClustering(input, 3);        
        diana.endClustering();
        
        // 5. Print the results
        diana.printTree(node); 
    }
    
    public static void main(final String[] args)
    {
        IrisExample s = new IrisExample();
    }
    
}