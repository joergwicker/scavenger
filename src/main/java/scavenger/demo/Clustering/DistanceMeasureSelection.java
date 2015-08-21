package scavenger.demo.clustering.distance;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Allows different distance measures with different weightings to be used. 
 * 
 * @see DataItem
 */
class DistanceMeasureSelection implements java.io.Serializable
{
    private String id; // Same as Key in data HashMap, which is found in DataItem.
    private List<String> ids = null; // Allows the same DistanceMeasure and weighting to be used for multiple values/features
    private DistanceMeasure distanceMeasure;
    private double weight = 1; // should be between 0 and 1


    /**
     *
     * @param id Used to identify the feature/value this distance measure and weight will be used on.
     * @param distanceMeasure
     * @param weight
     */
    public DistanceMeasureSelection(String id, DistanceMeasure distanceMeasure, double weight)
    {
        this.id = id;
        this.distanceMeasure = distanceMeasure;
        this.weight = weight;
    }
    
    /**
     *
     * @param ids Used to identify the features/values this distance measure and weight will be used on.
     * @param distanceMeasure
     * @param weight
     */
    public DistanceMeasureSelection(Iterator<String> id, DistanceMeasure distanceMeasure, double weight)
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
    
    /**
     * @return the id(s) as a list
     */
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