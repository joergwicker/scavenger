package scavenger.demo.clustering.distance;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.net.URL;
import java.util.Scanner;
import java.io.IOException;

class IrisExample
{   
    public IrisExample()
    {        
        List<DataInformation> dataInformationList = new ArrayList<DataInformation>();
        
        DistanceMeasure<List<Double>> distanceMeasure1 = new EuclideanDistance(3.6);//sepal_length
        DataInformation dataInfo1 = new DataInformation("sepal_length", distanceMeasure1, 1);
        dataInformationList.add(dataInfo1);
        
        DistanceMeasure<List<Double>> distanceMeasure2 = new EuclideanDistance(2.4);//sepal_width
        DataInformation dataInfo2 = new DataInformation("sepal_width", distanceMeasure2, 1);
        dataInformationList.add(dataInfo2);
        
        DistanceMeasure<List<Double>> distanceMeasure3 = new EuclideanDistance(5.9);//petal_length
        DataInformation dataInfo3 = new DataInformation("petal_length", distanceMeasure3, 1);
        dataInformationList.add(dataInfo3);
        
        DistanceMeasure<List<Double>> distanceMeasure4 = new EuclideanDistance(2.4);//petal_width
        DataInformation dataInfo4 = new DataInformation("petal_width", distanceMeasure4, 1);
        dataInformationList.add(dataInfo4);
        
        URL url;
        Scanner scan;
        //List<HashMap<String, Double>> data = new ArrayList<HashMap<String, Double>>();
        List<DataItem<List<Double>>> data = new ArrayList<DataItem<List<Double>>>();
        try
        {
            url = new URL("https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data");
            scan = new Scanner(url.openStream());
            
            while (scan.hasNext())
            {
                String temp = scan.next();
                String[] splitString = temp.split(",");
                HashMap<String, List<Double>> map = new HashMap<String, List<Double>>();
                
                List<Double> list1 = new ArrayList<Double>()
                {{ 
                    add(Double.parseDouble(splitString[0]));
                }};                
                map.put("sepal_length", list1);
                
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
                
                DataItem<List<Double>> item = new DataItem<List<Double>>(Integer.toString(data.size()), map);
                data.add(item);
            }
        }
        catch(IOException ex) 
        {
            ex.printStackTrace();
            System.exit(1);
        }
        
        TreeNode<List<Double>> input = new TreeNode<List<Double>>(data);
        Diana<List<Double>> diana = new Diana<List<Double>>(dataInformationList); 
        TreeNode<List<Double>> node = diana.runClustering(input, 3);
        printTree(node);
    }
    
    private void printTree(TreeNode<List<Double>> node)
    {
        Queue<TreeNode> queue = new LinkedList<TreeNode>();
        queue.add(node);
        while(!queue.isEmpty())
        {
            TreeNode r = queue.remove(); 
            r.print();
            if (r.getChildLeft() != null)
            {
                queue.add(r.getChildLeft());
                queue.add(r.getChildRight());
            }
        }
    }
    
    public static void main(final String[] args)
    {
        IrisExample s = new IrisExample();
    }
    
}