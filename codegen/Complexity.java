import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

public enum Complexity {
  
  DISTRIBUTED("Distributed", 1000),
  LOCAL("Local", 500),
  IRREDUCIBLE("Irreducible", 250),
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

  public static List<Complexity> inIncreasingOrder() {
    ArrayList<Complexity> a = new ArrayList<Complexity>();
    for (Complexity c : Complexity.values()) {
      a.add(c);
    }
    Collections.sort(a, new Comparator<Complexity>() {
      public int compare(Complexity a, Complexity b) {
        return a.index - b.index;
      }
    });
    return a;
  }

}