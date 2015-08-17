package scavenger.demo;

import scavenger.*;
import scavenger.app.ScavengerAppJ; //LocalScavengerAppJ

import java.util.function.Function;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
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
import akka.dispatch.*;
import akka.actor.*;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

/**
 * Example of how to use Scavenger to solve Sudoku
 *
 * @author Helen Harman
 */
class Sudoku extends ScavengerAppJ //LocalScavengerAppJ
{
    boolean solved = false;
    
    /**
     * Calls the super constructor. (Currently only used incase any code gets added to the super constructor)
     *
     * @param numWorkers Only used when running as a LocalScavengerAppJ.
     */
    public Sudoku(int numWorkers)
    {
        super();//numWorkers);  // numWorkers only used when running as a LocalScavengerAppJ    
    } 
    
    /**
     * Creates the Sudoku board
     *
     * @return The sudoku board 
     */
    private List<List<Integer>> createBoard()
    {
        List<List<Integer>> board = new ArrayList<List<Integer>>();
        
        Integer[][] temp =  { {4,0,0,0,6,0,7,0,1},
                              {6,8,0,5,0,0,0,9,0},
                              {1,9,0,0,0,4,5,0,2},
                              {8,0,0,1,0,0,0,4,0},
                              {0,0,0,6,0,0,9,0,0},
                              {9,0,0,0,0,3,0,0,8},
                              {0,0,0,3,0,0,0,0,4},
                              {0,0,0,0,5,0,0,3,0},
                              {7,0,0,0,1,8,0,5,0}
                            };
        /*Integer[][] temp =  { {4,3,5,2,6,9,7,0,1},
         {6,8,2,5,7,5,4,9,3},
         {1,9,7,0,3,4,5,6,2},
         {8,2,0,1,0,5,0,4,7},
         {3,0,4,6,0,2,9,0,5},
         {9,5,0,0,0,3,6,2,8},
         {5,0,9,3,2,0,8,7,4},
         {2,4,8,9,5,7,1,3,6},
         {7,6,3,4,1,8,2,5,0}
         };*/
        for (int i = 0; i < temp.length; i++)
        {            
            board.add(new ArrayList<Integer>());
            board.set(i, Arrays.asList(temp[i]));
        }
        return board;
    }
    
      
    /**
     * 
     * The following steps are performed until the sukoku board is solved (or is found to not be pssoible):
     *  1. Submits each board to FillKnowValues() as seperate jobs
     *  2. Waits for all jobs to return
     *  3. For each board/result returned :
     *      3.a : checks if sudoku is solved : if true - then exit
     *      3.b : checks if sudoku is possible : if false - then stop processing that board
     *      3.c : fills in a square with all possible values and adds these boards to possibleBoards
     */
    public void run()
    {
        List<List<Integer>> board = createBoard();        
        List<List<List<Integer>>> possibleBoards = new ArrayList<List<List<Integer>>>();
        possibleBoards.add(board);
        
        solved = false;
        
        while(!solved)
        {
            // For each possibleBoard submit a job to scavenger
            List<Future<List<List<Integer>>>> futures = new ArrayList<Future<List<List<Integer>>>>();
            for (List<List<Integer>> newBoard : possibleBoards)
            {
                System.out.println("making RunSudoku");
                ScavengerFunction<List<List<Integer>>> run = new FillKnowValues();
                Algorithm<List<List<Integer>>, List<List<Integer>>> algorithm = scavengerAlgorithm.expensive("id", run).cacheGlobally();
                Computation<List<List<Integer>>> computationData = scavengerComputation.apply("Computation_" + newBoard, newBoard).cacheGlobally();
                futures.add(scavengerContext().submit(algorithm.apply(computationData))); 
            }
            
            Future<Iterable<List<List<Integer>>>> allTogether = Futures.sequence(futures, scavengerContext().executionContext());
            
            // Wait for the results
            possibleBoards = new ArrayList<List<List<Integer>>>();
            Iterable<List<List<Integer>>> results = new ArrayList<List<List<Integer>>>();
            try 
            {                
                results = (Iterable<List<List<Integer>>>)Await.result(allTogether, (new Timeout(Duration.create(360, "seconds")).duration()));
            }
            catch(Exception e) 
            { 
                e.printStackTrace(); 
            }
            
            // For each board in the results :
            //  check if solved
            //  check if board is possible
            //  add all possible boards to list of boards to try and solve
            System.out.println("Results returned ... checking if solved");
            for (List<List<Integer>> result : results)
            {
                possibleBoards.addAll(processResult(result));
                if(solved)break;
                System.out.println("possibleBoards : " + possibleBoards.size());
            }
            
            if(possibleBoards.size() == 0)
            {
                System.out.println("FAILED : Not possible to solve board");
                break;
            }            
        }
        //System.out.println(scavengerContext().dumpCacheKeys());
        scavengerShutdown();    
    }
    
    /**
     * Alternative method for solving Sudoku
     * Using Stack and onSuccess, rather than waiting for all current jobs to finish.
     * This way Workers will always be busy, rather than waiting for all Workers to be finished.
     */
    public void runAlternative()
    {
        List<List<Integer>> board = createBoard();
        Stack<List<List<Integer>>> possibleBoards = new Stack<List<List<Integer>>>(); // Considered queue, but most complete boards might be a the top of the stack, so could finish quicker. (Could also be slower, if first boards are not possibles)
        List<Future<List<List<Integer>>>> futures = new ArrayList<Future<List<List<Integer>>>>();
        possibleBoards.push(board);
        
        solved = false;
        while(!solved)
        {
            if (!possibleBoards.empty())
            {
                List<List<Integer>> newBoard = possibleBoards.pop();
                
                ScavengerFunction<List<List<Integer>>> run = new FillKnowValues();
                Algorithm<List<List<Integer>>, List<List<Integer>>> algorithm = scavengerAlgorithm.expensive("id", run).cacheGlobally();
                Computation<List<List<Integer>>> computationData = scavengerComputation.apply("Computation_" + newBoard, newBoard).cacheGlobally();
                Future<List<List<Integer>>> future = scavengerContext().submit(algorithm.apply(computationData));
                            
                future.onSuccess(new OnSuccess<List<List<Integer>>>() 
                {
                    public void onSuccess(List<List<Integer>> result) 
                    {
                        List<List<List<Integer>>> boards = processResult(result);
                        for (List<List<Integer>> newBoard : boards)
                        {
                            possibleBoards.push(newBoard);   
                        }
                    }
                }, scavengerContext().executionContext());
                
                futures.add(future);
            }
            
            try
            {
                Thread.yield();
            }
            catch(Exception e) 
            { 
                e.printStackTrace(); 
            }            
            //TODO check if possible
        }
       scavengerShutdown();
    }
    
    
    /**
     * Checks if the board has been solved.
     * @return If not solved : finds the location with the least possible values, 
     *                              and returns the boards with that location filled in. (@see bestGuessBoards())
     *         If solved     : returns empty list
     */
    private List<List<List<Integer>>> processResult(List<List<Integer>> result)
    {
        List<List<List<Integer>>> possibleBoards = new ArrayList<List<List<Integer>>>();
        System.out.println("Checking ...");
        if (SudokuUtils.isSolved(result))
        {
            System.out.println("Solved");
            SudokuUtils.printBoard(result);
            solved = true;
        }
        else 
        {      
            Iterable<Location> locations = findPossibleValues(result);
            
            if(isPossible(locations))
            {
                possibleBoards.addAll(bestGuessBoards(locations, result));
            }
        }
        return possibleBoards;
    }
    
    /**
     * @param locations An list of the locations not currently filled in
     * @return If any of the location contain 0 possiblities, then the sudoku is not solvable
     */
    private boolean isPossible(Iterable<Location> locations)
    {
        for (Location location : locations)
        { 
            if((location.possibleValues.size() == 0))
            {
                System.out.println("not possible for : " + location.x + ", " + location.y);
                return false; 
            }
        }
        return true;
    }
    
    /**
     * Find the location with the least possible values and create a board for each of those values
     * 
     * @param locations
     * @param board
     * @return A list of boards with the different values for the found location filled in.
     */
    private List<List<List<Integer>>> bestGuessBoards(Iterable<Location> locations, List<List<Integer>> board)
    {
        // Find the location with the least possible values
        Location locClosest = new Location(0,0,0);
        for (Location loc : locations)
        {
            if (loc.possibleValues.size() < locClosest.possibleValues.size())
            {
                locClosest = loc;
            }
            if (locClosest.possibleValues.size() == 2)break;
        }
        
        // Create the the boards
        List<List<List<Integer>>> boards = new ArrayList<List<List<Integer>>>();
        for (Integer possibleValue : locClosest.possibleValues)
        {
            List<List<Integer>> newBoard = new ArrayList<List<Integer>>();
            for(int i = 0; i < board.size(); i++)
            {                
                newBoard.add(new ArrayList<Integer>());
                for (int j = 0; j < board.get(i).size(); j++)
                {
                    newBoard.get(i).add(new Integer(board.get(i).get(j)));
                }
            }
            
            newBoard.get(locClosest.x).set(locClosest.y, possibleValue);
            boards.add(newBoard);
        }   
        return boards;
    }
    
     /**
      * Finds all possible values for each square on the board.
      * This is also done by submitting jobs to scavenger, as each location's values can be found idependently.  
      * 
      * @param board
      * @return A list of locations (which contain the possible values @see Location.java)
      */
    private Iterable<Location> findPossibleValues(List<List<Integer>> board)
    {
        System.out.println("running findPossibleValues()");
        
        List<Future<Location>> futures = new ArrayList<Future<Location>>();
        for(int i = 0; i < board.size(); i++)
        {
            for(int j = 0; j < board.get(i).size(); j++)
            {
                if(board.get(i).get(j) == 0)
                {
                    ScavengerFunction<Location> fillLocation = new FindPossibleValues(board);
                    Algorithm<Location, Location> algorithm = scavengerAlgorithm.expensive("id", fillLocation).cacheGlobally();
                    Location loc = new Location(i, j, board.get(i).get(j));
                    Computation<Location> computationData = scavengerComputation.apply("Computation_" + i + j, loc).cacheGlobally();
                    futures.add(scavengerContext().submit(algorithm.apply(computationData)));
                }
            }
        }
        
        Future<Iterable<Location>> allTogether = Futures.sequence(futures, scavengerContext().executionContext());
        Iterable<Location> results = new ArrayList<Location>();
        try
        {
            System.out.println("findPossibleValues : Waiting for results");
            results = (Iterable<Location>)Await.result(allTogether, (new Timeout(Duration.create(60, "seconds")).duration()));
        }
        catch(Exception e) 
        { 
            e.printStackTrace(); 
        }        
        return results;
    }
        
    /**
     * 
     */    
    public static void main(final String[] args)
    {    
        Sudoku sudoku = new Sudoku(4);
        sudoku.run(); // using list and Await
        //sudoku.runAlternative(); // using Stack and onSuccess
    }
}
