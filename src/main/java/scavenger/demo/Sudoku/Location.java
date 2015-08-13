package scavenger.demo;

import java.util.ArrayList;
import java.util.List;


class Location implements java.io.Serializable
{
    public int x;
    public int y;
    //public int value;
    public List<Integer> possibleValues;
    
    public Location(int x, int y, int value)
    {
        this.x = x;
        this.y = y;
        //this.value = value;
        if (value == 0)
        {
            this.possibleValues = new ArrayList<Integer>() {{
                for(int i = 1; i <= 9; i++) add(i);
            }}; //{1,2,3,4,5,6,7,8,9};
        } 
        else
        {
            this.possibleValues = new ArrayList<Integer>() {{
                add(value);
            }};
        }
    }
}

