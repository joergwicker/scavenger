public enum Complexity {
  
  DISTRIBUTED("Distributed", 1000),
  LOCAL("Local", 500),
  TRIVIAL("Trivial", 50);

  public final String prefix;
  public final int index;
  private Complexity(String prefix, int index) {
    this.prefix = prefix;
    this.index = index;
  }
  public String toLowerCase() {
    return prefix.toLowerCase();
  }
}