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


class SudokuFunc extends ScavengerFunction<Location> implements java.io.Serializable
{
    protected package$ scavengerAlgorithm = package$.MODULE$;
    protected Computation$ scavengerComputation = Computation$.MODULE$;
    private List<List<Integer>> board;
    int BOARD_SIZE = 9;
    int BOARD_SECTION_SIZE = 3;
    public SudokuFunc(List<List<Integer>> board)
    {
        this.board = board;
    }
    
    // Value computation is being applied to can be accessed using "this.value"
    // To submit a new job this.ctx.submit(...) can be used
    public Location call() 
    {
        if (value.possibleValues.size() == 1)
        {
            return value;
        }
        checkPossibleValues();
        return value;
    }
    
    private void checkPossibleValues()
    {
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
