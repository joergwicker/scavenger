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


class RunSudoku extends ScavengerFunction<List<List<Integer>>>
{
    protected package$ scavengerAlgorithm = package$.MODULE$;
    protected Computation$ scavengerComputation = Computation$.MODULE$;
    private Context scavengerContext;
    protected List<List<Integer>> board;
    
    /**
     *
     */
    public List<List<Integer>> call() 
    {
        board = value;
        while (!SudokuUtils.isSolved(board))
        {
            if(!fillInKnownValues())
            {
                return board; 
            }
        }
        return board;
    }
    
    /**
     *
     */
    private boolean fillInKnownValues()
    {
        System.out.println("Running : fillInKnownValues()");
        Iterable<Location> locations = SudokuUtils.findPossibleValues(board, ctx);        
        boolean changed = false;
        for (Location location : locations)
        {
            if(location.possibleValues.size() == 1)
            {
                board.get(location.x).set(location.y, location.possibleValues.get(0));
                changed = true;
            }
            
        }
        return changed;
    }
}

    


