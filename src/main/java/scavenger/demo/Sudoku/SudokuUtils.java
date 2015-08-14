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
    /**
     *
     */
    protected static boolean isSolved(List<List<Integer>> board)
    {
        printBoard(board);
        for(int i = 0; i < board.size(); i++)
        {
            for(int j = 0; j < board.get(i).size(); j++)
            {
                if(board.get(i).get(j) == 0)
                {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     *
     */
    protected static void printBoard(List<List<Integer>> board)
    {
        for(int i = 0; i < board.size(); i++)
        {
            for(int j = 0; j < board.get(i).size(); j++)
            {
                System.out.print(board.get(i).get(j) + ", ");
            }
            System.out.println("");
        }
    }

}

