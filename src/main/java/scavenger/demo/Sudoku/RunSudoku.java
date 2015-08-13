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
    
    
    //int BOARD_SIZE = 9;
    protected List<List<Integer>> board;
    public List<List<Integer>> call() 
    {
        board = value;
        while (!SudokuUtils.isSolved(board))
        {
            if(!fillBoardKnown())
            {
                return board; 
            }
        }
        return board;
    }
    
    private boolean fillBoardKnown()
    {
        System.out.println("fillBoardKnown");
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
    
    /**
     *
     */
   /* private Iterable<Location> findPossibleValues(List<List<Integer>> board)
    {
        System.out.println("running findPossibleValues()");
        
        List<Future<Location>> futures = new ArrayList<Future<Location>>();
        for(int i = 0; i < BOARD_SIZE; i++)
        {
            for(int j = 0; j < BOARD_SIZE; j++)
            {
                if(board.get(i).get(j) == BOARD_SIZE)
                {
                    ScavengerFunction<Location> fillLocation = new SudokuFunc(board);
                    Algorithm<Location, Location> algorithm = scavengerAlgorithm.expensive("id", fillLocation).cacheGlobally();
                    System.out.println("findPossibleValues : create future");
                    Location loc = new Location(i, j, board.get(i).get(j));
                    Computation<Location> computationData = scavengerComputation.apply("Computation_" + i + j, loc).cacheGlobally();
                    futures.add(ctx.submit(algorithm.apply(computationData)));
                }
            }
        }
        
        Future<Iterable<Location>> allTogether = Futures.sequence(futures, ctx.executionContext());
        
        Iterable<Location> results = new ArrayList<Location>();
        try
        {
            System.out.println("findPossibleValues : await");
            results = (Iterable<Location>)Await.result(allTogether, (new Timeout(Duration.create(60, "seconds")).duration()));
        }
        catch(Exception e) 
        { 
            e.printStackTrace(); 
        }
        
        return results;
    }*/
    
    /**
     *
     */
    
    
    /**
     *
     */
   /* private boolean isSolved(List<List<Integer>> board)
    {
        System.out.println("is solved ?");
        for(int i = 0; i < BOARD_SIZE; i++)
        {
            for(int j = 0; j < BOARD_SIZE; j++)
            {
                if(board.get(i).get(j) == 0)
                {
                    return false;
                }
            }
        }
        
        // prints out the results if it is solved
        for(int i = 0; i < BOARD_SIZE; i++)
        {
            for(int j = 0; j < BOARD_SIZE; j++)
            {
                System.out.print(board.get(i).get(j) + ", ");
            }
            System.out.println("");
        }
        
        return true;
    }*/
    


