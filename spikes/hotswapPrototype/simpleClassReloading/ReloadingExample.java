

public class ReloadingExample {

  private static ReloadableAlgorithm reloadAlgorithm()
  throws Exception {
    ClassLoader interfaceLoader = ReloadableAlgorithm.class.getClassLoader();
    ClassLoader singleUse = new SingleUseClassLoader(interfaceLoader);
    Class<?> algoClass = singleUse.loadClass("ConvergeSomewhere");
    return (ReloadableAlgorithm)algoClass.newInstance();
  }

  public static void main(String[] args) throws Exception {
    ReloadableAlgorithm algo = null;
    double x = 0.5;
    for (int i = 0; i < 100; i++) {
      if (i % 10 == 0) {
        algo = reloadAlgorithm();
      }
      x = algo.doSomething(x);
      System.out.println("Iteration: " + i + " x = " + x + " algo: " + algo);
    }
  }
}