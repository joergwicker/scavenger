package scavenger;

import scavenger.*;
import scala.Function2;
import scala.runtime.AbstractFunction2;

import scala.concurrent.Future;
import static akka.dispatch.Futures.future;
import java.util.concurrent.Callable;


public abstract class ScavengerFunction<X> extends AbstractFunction2<X, Context, Future<X>> implements java.io.Serializable, Callable<X>
{
    protected X value;
    protected Context ctx;
    public Future<X> apply(X value, Context ctx) 
    {
        this.value = value;
        this.ctx = ctx;
        Callable<X> f = this;
        return future(f, ctx.executionContext());
    } 
}

