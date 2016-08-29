import java.lang.ClassLoader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class MyClassLoader extends ClassLoader {

  public MyClassLoader(ClassLoader parentClassLoader) {
    super(parentClassLoader);
  }

  Class<?> loadedBaz = null;

  public Class<?> loadClass(String binaryName) 
  throws ClassNotFoundException {

    System.out.println("Asked to load " + binaryName);

    // defineClass(String name, byte[] b, int off, int len)
    // The `defineClass` method is implemented in the abstract superclass,
    // it transforms the byte array into a class definition
    
    if ("Baz".equals(binaryName)) {
      if (loadedBaz == null) {
        try {
          File inputFile = new File("Baz.class");
          byte[] bytes = Files.readAllBytes(inputFile.toPath());
          loadedBaz = defineClass("Baz", bytes, 0, bytes.length);
        } catch (IOException e) {
          System.err.println("Could not read file.");
          throw new ClassNotFoundException();
        }
      }
      return loadedBaz;
    } else {
      return getParent().loadClass(binaryName);
    }
  }

  protected Class<?> findClass(String fullName) 
  throws ClassNotFoundException {
    System.out.println("Finding " + fullName);
    if ("Baz".equals(fullName)) {
      return loadedBaz;
    } else {
      return super.findClass(fullName);
    }
  }

  public static void main(String[] args) {
    // otherwise: "class Baz cannot access its superinterface Bazish"
    ClassLoader interfaceClassLoader = Bazish.class.getClassLoader();
    MyClassLoader mcl = new MyClassLoader(interfaceClassLoader);
    Bazish baz = null;
    try {
      Class<?> bazClass = mcl.loadClass("Baz");
      baz = (Bazish)(bazClass.newInstance());
    } catch (ClassNotFoundException e) {
      System.out.println("Could not load `Baz` class");
      System.exit(1);
    } catch (InstantiationException e) {
      System.out.println("Could not instantiate object of type `Baz`");
      System.exit(2);
    } catch (IllegalAccessException e) {
      System.out.println("Cannot access the nullary constructor of `Baz`");
      System.exit(3);
    }

    if (baz != null) {
      baz.baz(); 
    } else {
      System.out.println("baz is null");
    }
  }
}