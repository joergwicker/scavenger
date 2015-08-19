package scavenger.demo.clustering.distance;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

class EuclideanDistance implements DistanceMeasure<List<Double>>
{
    private double maxDifference; // The maximum difference between two values
    
    public EuclideanDistance()
    {
        this.maxDifference = 1;
    }
    
    public EuclideanDistance(double maxDifference)
    {
        this.maxDifference = maxDifference;
    }
    
    public double getDistance(List<Double> value1, List<Double> value2)
    {
        if (value1.size() != value2.size())
        {
            return 0.0; //TODO throw exeception
        }
        double total = 0.0;
        for(int i = 0; i < value1.size(); i++)
        {
             total = total + Math.pow((Math.max(value1.get(i), value2.get(i)) - Math.min(value1.get(i), value2.get(i))), 2);       
        }
        return (Math.sqrt(total) / Math.sqrt(value1.size()) / maxDifference);
    }
    
    public static void main(final String[] args)
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
    }
}

