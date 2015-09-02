package scavenger.demo.clustering.distance;
import scavenger.demo.clustering.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;


/**
 * Used to calculate the euclidean distance between two lists
 */
public class EuclideanDistance extends DistanceMeasure<List<Double>>
{
    private double maxDifference; // The maximum difference between two values
    
    /**
     * maxDifference is set to default value (1).
     */
    public EuclideanDistance()
    {
        this.maxDifference = 1;
    }
    
    /**
     *
     * @param maxDifference
     */
    public EuclideanDistance(double maxDifference)
    {
        this.maxDifference = maxDifference;  
    }
    
    /**
     *
     * @param value1
     * @param value2
     *
     * @return the euclidean distance between value1 and value2, normalised (between 0 and 1).
     */
    public double calculateDistance(List<Double> value1, List<Double> value2)
    {
        if (value1.size() != value2.size())
        {
            return 0.0;
        }
        double total = 0.0;
        double totalMaxDifference = 0;
        for(int i = 0; i < value1.size(); i++)
        {
             total = total + Math.pow((Math.max(value1.get(i), value2.get(i)) - Math.min(value1.get(i), value2.get(i))), 2);      
        }
        
        double sigmoidValue = sigmoid(Math.sqrt(total) / Math.sqrt(maxDifference * value1.size()));/// Math.sqrt(value1.size()) / maxDifference);
        //System.out.println("sigmoidValue : " + sigmoidValue);
        return sigmoidValue;
    }
    
    private double sigmoid(double x)
    {
        double result = 1 / (1 + Math.exp(-x));
        result = (result - 0.5) / 0.5; // x will never be negative, so sigmoid will be between 0.5 and 1
        return result;
    }
    
    
    /**
     * For testing
     */
   /* public static void main(final String[] args)
    {
        List list1 = new ArrayList<Double>() 
            {{
                add(5.0);
                add(8.0);
            }};
            
       List list2 = new ArrayList<Double>() 
            {{
                add(2.0);
                add(9.0);
            }};     
       EuclideanDistance e = new EuclideanDistance(10.0);
       System.out.println(e.getDistance(list1, list2));
    }*/
}


