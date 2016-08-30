public class Baz implements Bazish {
  int state = 0;
  public Baz() {
    state = 42;
  }
  public void baz() {
    System.out.println("Square of " + state + " = " + state * state);
  }
}