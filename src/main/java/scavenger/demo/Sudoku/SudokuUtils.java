package scavenger.demo;

import scavenger.*;
import scavenger.app.ScavengerAppJ;

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


class SudokuUtils
{
    protected static int BOARD_SIZE = 9;    
    
    protected static Iterable<Location> findPossibleValues(List<List<Integer>> board, Context scavengerContext)
    {
        System.out.println("running findPossibleValues()");
        package$ scavengerAlgorithm = package$.MODULE$;
        Computation$ scavengerComputation = Computation$.MODULE$;
        
        List<Future<Location>> futures = new ArrayList<Future<Location>>();
        for(int i = 0; i < 9; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                if(board.get(i).get(j) == 0)
                {
                    ScavengerFunction<Location> fillLocation = new SudokuFunc(board);
                    Algorithm<Location, Location> algorithm = scavengerAlgorithm.expensive("id", fillLocation).cacheGlobally();
                    System.out.println("findPossibleValues : create future");
                    Location loc = new Location(i, j, board.get(i).get(j));
                    Computation<Location> computationData = scavengerComputation.apply("Computation_" + i + j, loc).cacheGlobally();
                    futures.add(scavengerContext.submit(algorithm.apply(computationData)));
                }
            }
        }
        
        Future<Iterable<Location>> allTogether = Futures.sequence(futures, scavengerContext.executionContext());
        
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
    }
    
    
    protected static boolean isSolved(List<List<Integer>> board)
    {
        printBoard(board);
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
        return true;
    }
    
    
    protected static void printBoard(List<List<Integer>> board)
    {
        for(int i = 0; i < BOARD_SIZE; i++)
        {
            for(int j = 0; j < BOARD_SIZE; j++)
            {
                System.out.print(board.get(i).get(j) + ", ");
            }
            System.out.println("");
        }
    }

}

