package scavenger.demo.clustering.distance;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

class DataInformation
{
    private String id;
    private List<String> ids = null;
    private DistanceMeasure distanceMeasure;
    private double weight = 1; // should be between 0 and 1

    public DataInformation(String id, DistanceMeasure distanceMeasure, double weight)
    {
        this.id = id;
        this.distanceMeasure = distanceMeasure;
        this.weight = weight;
    }
    
    public DataInformation(Iterator<String> id, DistanceMeasure distanceMeasure, double weight)
    {
        this.ids = ids;
        this.distanceMeasure = distanceMeasure;
        this.weight = weight;    
    }   
    
    
    public DistanceMeasure getDistanceMeasure()
    {
        return distanceMeasure;
    }
    
    public String getId()
    {
        return id;
    }
    
    
    public List<String> getIds()
    {
        if((ids == null) || (ids.size() == 0))
        {
            List<String> list = new ArrayList<String>();
            list.add(id);
            return list;
        }
        else
        {
            return ids;
        }
    }
    
    public double getWeight()
    {
        return weight;
    }
    
}