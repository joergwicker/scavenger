public class ConvergeZero implements ReloadableAlgorithm {
  public double doSomething(double x) {
    return x * 0.99;
  }
  @Override
  public String toString() {
    return "-> 0";
  }
}