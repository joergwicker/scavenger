public class Main {
  public static void main(String[] args) {
    IterativeAlgorithm algo = new IterativeAlgorithm();
    algo.initialize();
    for (int i = 0; i < 20; i++) {
      algo.run(5);
      algo.showState();
      if (i == 10) {
      	algo.reload();
      }
    }
  }
}