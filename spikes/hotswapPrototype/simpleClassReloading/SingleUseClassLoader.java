import java.lang.ClassLoader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SingleUseClassLoader extends ClassLoader {
  public SingleUseClassLoader(ClassLoader parent) {
    super(parent);
  }

  public Class<?> loadClass(String fullName) 
  throws ClassNotFoundException {
    if ("ConvergeSomewhere".equals(fullName)) {
      String actualName = 
        (Math.random() > 0.5) ? "ConvergeOne" : "ConvergeZero";
      try {
        File inputFile = new File(actualName + ".class");
        byte[] bytes = Files.readAllBytes(inputFile.toPath());
        return defineClass(actualName, bytes, 0, bytes.length);
      } catch (IOException e) {
        System.err.println("Could not read file for " + actualName);
        throw new ClassNotFoundException();
      }
    } else {
      return getParent().loadClass(fullName);
    }
  }
}