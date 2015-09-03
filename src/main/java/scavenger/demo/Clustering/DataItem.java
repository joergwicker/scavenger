package scavenger.demo.clustering;
import scavenger.demo.clustering.distance.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A single record of data.
 * Used to hold the features of the item of data.
 * Features can either be stored in a list, as a HashMap, or as a single value.
 *
 * @author Helen Harman
 */
public class DataItem<T> implements java.io.Serializable
{
    private String id = "not_set";
    private List<T> dataList = null;
    private HashMap<String, T> dataAsHash = null;
    private T data = null;

    ////// Constructors ///////
    
    /**
     * 
     * @param id Used to identify this DataItem. 
     * @param data A list containing the data (eg. Features) 
     */
    public DataItem(String id, List<T> data)
    {
        this.id = id;
        this.dataList = data;
    }
    
    /**
     * 
     * @see DistanceMeasureSelection
     * @param id Used to identify this DataItem. 
     * @param data A HashMap containing (Key = used to identify which distance measure to use, Value = the data (eg. Features))
     */
    public DataItem(String id, HashMap<String, T> data)
    {
        this.id = id;
        this.dataAsHash = data;
    }
    
    /**
     * 
     * @param id Used to identify this DataItem. 
     * @param data The data (eg. A feature) 
     */
    public DataItem(String id, T data)
    {
        this.id = id;
        this.data = data;
    }
    
    /**
     *  
     * @param data A list containing the data (eg. Features) 
     */
    public DataItem(List<T> data)
    {
        this.dataList = data;
    }
    
    /**
     * 
     * @see DistanceMeasureSelection
     * @param data A HashMap containing (Key = used to identify which distance measure to use, Value = the data (eg. Features))
     */
    public DataItem(HashMap<String, T> data)
    {
        this.dataAsHash = data;
    }
    
    /**
     * 
     * @param id Used to identify this DataItem. 
     * @param data The data (eg. A feature) 
     */
    public DataItem(T data)
    {
        this.data = data;
    }
    
    ////////////////////////
    
    /**
     * @return True if a HashMap is beign used
     */
    public boolean isHash()
    {
        return (dataAsHash != null);
    }
    
    /**
     * @return The data
     */
    public Object getData()
    {
        if (isHash())
        {
            List<T> list = new ArrayList<T>(dataAsHash.values());
            return list.get(0);//only one distance measure being used, so HashMap should only have one value
        }
        else if (dataList != null)
        {
            return dataList;
        }
        else 
        {
            return data;
        }
    }
    
    /**
     * @return A HashMap containing (Key = used to identify which distance measure to use, Value = the data (eg. Features))
     */
    public HashMap<String, T> getHashMap()
    {
        return dataAsHash;
    }
    
    /**
     * @return String used to identify this DataItem.
     */
    public String getId()
    {
        return id;
    }
}