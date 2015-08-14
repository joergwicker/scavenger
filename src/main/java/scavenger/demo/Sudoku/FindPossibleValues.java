package scavenger.demo;

import scavenger.*;
import scavenger.app.LocalScavengerAppJ;

import java.util.function.Function;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.lang.reflect.Array;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.Future;

import akka.dispatch.Futures;
import akka.util.Timeout;
import static akka.dispatch.Futures.future;
import static akka.dispatch.Futures.sequence;


class FindPossibleValues extends ScavengerFunction<Location>
{
    private List<List<Integer>> board;
    
    /**
     *
     */
    public FindPossibleValues(List<List<Integer>> board)
    {
        this.board = board;
    }
    
    public void setValue(Location loc)
    {
        value = loc;
    }

    /**
     *
     */
    public Location call() 
    {
        if (value.possibleValues.size() == 1)
        {
            return value;
        }
        checkPossibleValues();
        return value;
    }
    
    /**
     *
     */
    private void checkPossibleValues()
    {
        int BOARD_SIZE = board.size();
        int BOARD_SECTION_SIZE = (int)java.lang.Math.sqrt((double)BOARD_SIZE);    
        int offSetRow = (value.x / BOARD_SECTION_SIZE)*BOARD_SECTION_SIZE;
        int offSetCol = (value.y / BOARD_SECTION_SIZE)*BOARD_SECTION_SIZE;
        List<Integer> newPossibleValues = new ArrayList<Integer>();
        for (Integer i : value.possibleValues)
        {
            boolean add = true;
            for(int j = 0; j < BOARD_SIZE; j++)
            {
                if ((board.get(value.x).get(j).intValue() == i.intValue()) || 
                    (board.get(j).get(value.y).intValue() == i.intValue()) || 
                    (board.get((offSetRow + (j / BOARD_SECTION_SIZE))).get((offSetCol + (j % BOARD_SECTION_SIZE))).intValue() == i.intValue())
                    )
                {
                    add = false;                            
                    break;
                }
            }
            if (add)
            {
                newPossibleValues.add(i);
            }
        }
        
        value.possibleValues = newPossibleValues;
    }        
}
