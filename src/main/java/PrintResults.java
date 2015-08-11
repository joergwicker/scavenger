package scavenger.demo;


import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import static akka.pattern.Patterns.ask;
import akka.dispatch.*;
public final class PrintResults<T> extends OnSuccess<T> {
    
    @Override
    public final void onSuccess(T t) {
        Iterable<Integer> results = (Iterable<Integer>)t;
        for (Integer i : results)
        {
            System.out.println("PrintResults says: " + ((Integer)i));
        }
    }
}