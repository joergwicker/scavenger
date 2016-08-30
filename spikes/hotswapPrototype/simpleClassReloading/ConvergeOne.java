public class ConvergeOne implements ReloadableAlgorithm {
  public double doSomething(double x) {
    return Math.sqrt(x);
  }
  @Override
  public String toString() {
    return "-> 1";
  }
}