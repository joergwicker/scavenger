class IterativeAlgorithm {
	private IterationStep stepStrategy;
	private boolean reloadNecessary = false;

    private int stepIdx = 0;
    private double state = 1.0;

    public void showState() {
      System.out.println("Current state at step " + stepIdx + ": " + state);
    }

	public void reload() {
      if (reloadNecessary) {
        stepStrategy = HotSwapClassLoader.reloadIterationStep();
        reloadNecessary = false;
      }
	}

	public run(int steps) {
	  for (int i = 0; i < steps; i++) {
        stepIdx++;
        state = stepStrategy.doSomething(state);
	  }
	}
}