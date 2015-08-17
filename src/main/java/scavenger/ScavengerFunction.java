package scavenger;

import scavenger.*;
import scala.Function2;
import scala.runtime.AbstractFunction2;

import scala.concurrent.Future;
import static akka.dispatch.Futures.future;
import java.util.concurrent.Callable;

/**
 * Used to create a Function that can be submited to a Worker.
 * value is the data that the computation is to be performed on.
 *
 * For an example of how to use this class see : Sudoku.java
 *
 * @author Helen Harman
 */
public abstract class ScavengerFunction<X> extends AbstractFunction2<X, Context, Future<X>> implements java.io.Serializable, Callable<X>
{
    protected X value; // what the computation will be performed on
    protected Context ctx; // the context to submit jobs and futures to
    
    /**
     * Creates the Future which is submitted to scavenger
     * 
     * @param value data the computation will be performed on
     * @param ctx context the jobs and futures will be submitted to
     * @return This object as a Future submitted to the given Context (eg. scavengerContext)
     */
    public Future<X> apply(X value, Context ctx) 
    {
        this.value = value;
        this.ctx = ctx;
        Callable<X> f = this;
        return future(f, ctx.executionContext());
    } 
}

