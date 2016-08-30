package dangerous;

/** 
 * Used in a classloader test-case in `IsolatingClassLoaderSpec`
 */
public class LoadMe {
  public LoadMe() {
    // do nothing
  }
  @Override
  public String toString() {
    return "Grrr, I'm dangerous!";
  }
}
