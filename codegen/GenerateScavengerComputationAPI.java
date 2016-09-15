import java.util.*;
import java.io.*;
import static java.lang.System.*;

/**
 * Generates the source code of the related traits
 * `Computation`, `LocalComputation`, `SimpleComputation`, `TrivialComputation`.
 *
 * This Java program is compiled and executed during the build phase 
 * of the Scavenger 2.x project. 
 * It generates the source code of the four traits mentioned above,
 * which is than compiled with the rest of the project.
 *
 * We generate code because we want to provide 
 * an interface that can be easily used from Java in a type safe way.
 * Therefore, we do not rely on any implicits and  `CanBuildFrom`-patterns, 
 * but use plain old overloading instead. This results in some boilerplate.
 * The present program generates the boilerplate code.
 *
 * As argument, it takes the path to the directory in which to put 
 * the generated source files (together with `/` in the end) 
 * (sbt and maven might use different directories for the same purpose).
 * 
 * It then grabs some of the templates in `/src/main/codegen/`, 
 * fills out the gaps, and saves the generated files in 
 * `${sourceManaged}/main/scala/`.
 */
public class GenerateScavengerComputationAPI {

  public static LinkedList<Template> generateCode() 
  throws IOException {
    LinkedList<Template> result = new LinkedList<Template>();

    for (int i : Complexity.values()) {
      Template t = new Template(
        "main/codegen-scala/scavenger/Id.scala"
      );
      result.add(t.subst("body", "/* a comment for " + i + " */"));
    }
    return result;
  }

  public static void main(String[] args) {
    out.println("Code generation runs");
    String pathToSourceManaged = args[0];
    try {
      for (Template sd: generateCode()) {
        out.println("Generating " + sd.relativePath);
        sd.saveTo(pathToSourceManaged);
      }
    } catch (IOException e) {
      out.println("Exception occured during source code generation: ");
      e.printStackTrace();
      out.println("Source code generation failed. Exit.");
      System.exit(1);
    }
  }
}