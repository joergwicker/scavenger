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


class Sudoku extends ScavengerAppJ
{
    //List<List<Integer>> board;
    //int BOARD_SIZE = 9;
    boolean solved = false;
    
    public Sudoku(int numWorkers)
    {
        super();//numWorkers);        
    } 
    
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
     */
    public void run()
    {
        List<List<Integer>> board = createBoard();
        List<List<List<Integer>>> possibleBoards = new ArrayList<List<List<Integer>>>();
        possibleBoards.add(board);
        boolean solved = false;
        while(!solved)
        {
            // For each possibleBoard submit a job to scavenger
            List<Future<List<List<Integer>>>> futures = new ArrayList<Future<List<List<Integer>>>>();
            for (List<List<Integer>> newBoard : possibleBoards)
            {
                System.out.println("making RunSudoku");
                ScavengerFunction<List<List<Integer>>> run = new RunSudoku();//this.scavengerContext);
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
                System.out.println("Checking ...");
                if (SudokuUtils.isSolved(result))
                {
                    System.out.println("Solved");
                    board = result;
                    solved = true;
                    break;
                }
                else 
                {      
                    Iterable<Location> locations = SudokuUtils.findPossibleValues(result, scavengerContext());
                    
                    if(isPossible(locations))
                    {
                        possibleBoards.addAll(bestGuessBoards(locations, result));
                    }
                }
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
     * 
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
     * Find the location with the least possible values
     * @return A list of boards with the different values for that location filled in.
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
     * 
     */    
    public static void main(final String[] args)
    {    
        Sudoku sudoku = new Sudoku(4);
        sudoku.run();
    }
}
