package scavenger.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a square on the sudoku board. Contains a list of possible values for the square.
 *
 * @author Helen Harman
 */
class Location implements java.io.Serializable
{
    public int x;
    public int y;
    public List<Integer> possibleValues;
    
    public Location(int x, int y, int value)
    {
        this.x = x;
        this.y = y;
        
        // if no value given, then add 0-9 as possible values
        if (value == 0)
        {
            this.possibleValues = new ArrayList<Integer>() 
            {{
                for(int i = 1; i <= 9; i++) add(i);
            }};
        } 
        else
        {
            this.possibleValues = new ArrayList<Integer>() 
            {{
                add(value);
            }};
        }
    }
}

