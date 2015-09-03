package scavenger.demo.clustering.distance;
import scavenger.demo.clustering.*;
import java.util.List;
import java.util.Arrays;
import java.lang.Math;

/**
 * Used to calculate the Tanimoto distance between two BitSets
 */
public class OrdinalStringDistance extends DistanceMeasure<String>
{
    
    private List<String> valueOrder;
    
    public OrdinalStringDistance(List<String> valueOrder)
    {
        this.valueOrder = valueOrder;
    }
      
       
        
    /**
     * 
     * @param value1
     * @param value2
     * @return Distance between value1 and value2
     */
    public double calculateDistance(String value1, String value2)
    {
        double indexValue1 = valueOrder.indexOf(value1);
        double indexValue2 = valueOrder.indexOf(value2);
        
        
        double difference = Math.abs(indexValue1 - indexValue2);
        
        
        double distance = Math.pow(difference, 2) / Math.pow(valueOrder.size(), 2);
        //System.out.println("calculateDistance " + value1 + " and " + value2 + " = " + distance);
        return distance;
    }
    
 
}