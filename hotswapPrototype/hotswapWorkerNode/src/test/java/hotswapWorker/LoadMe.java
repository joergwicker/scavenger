package hotswapWorker;

/** 
 * Used in a classloader test-case in `IsolatingClassLoaderSpec`
 */
public class LoadMe {
  public LoadMe() {
    // do nothing
  }
  @Override
  public String toString() {
    return "I am a Java class";
  }
}