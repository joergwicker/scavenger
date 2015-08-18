package scavenger.demo.clustering.distance;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;


class DataItem<T>
{
    private String id = "not_set";
    private List<T> dataList = null;
    private HashMap<String, T> dataAsHash = null;
    private T data = null;

    public DataItem(String id, List<T> data)
    {
        this.id = id;
        this.dataList = data;
    }
    
    public DataItem(String id, HashMap<String, T> data)
    {
        this.id = id;
        this.dataAsHash = data;
    }
    
    public DataItem(String id, T data)
    {
        this.id = id;
        this.data = data;
    }
    
    public DataItem(List<T> data)
    {
        this.dataList = data;
    }
    
    public DataItem(T data)
    {
        this.data = data;
    }
    
    public DataItem(HashMap<String, T> data)
    {
        this.dataAsHash = data;
    }
    
    public boolean isHash()
    {
        return (dataAsHash != null);
    }
    
    public Object getData()
    {
        if (isHash())
        {
            List<T> list = new ArrayList<T>(dataAsHash.values());
            return list;
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
    
    public HashMap<String, T> getHashMap()
    {
        return dataAsHash;
    }
    
    public String getId()
    {
        return id;
    }
}