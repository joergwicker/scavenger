import java.util.*;
import java.io.*;
import static java.lang.System.*;

/**
 * Generates the source code of the API-traits
 * `DistributedJob`, `LocalJob`, `TrivialComputation` and the like.
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
 *
 * The generated API does NOT include: 
 * - size estimations 
 * - resoure identification (only Strings for atomic algorithms and resources)
 * - any evaluation
 * - anything compression-related
 * - any explicit caching and stuff like that
 * What it does include:
 * - interface-traits
 * - case classes for formal job description
 * - methods that allow to conveniently compose subjobs, and always get
 *   the result of the right type (at compile time and at runtime)
 */
public class GenerateScavengerComputationAPI {

  public static LinkedList<Template> generateCode() 
  throws IOException {
    LinkedList<Template> result = new LinkedList<Template>();

    for (Complexity complexity : Complexity.values()) {

      // Generate `<<X>>Job.scala`
      Template xJob = new Template("main/codegen-scala/scavenger/XJob.scala");
      String _extends = generateExtends(complexity, "Job");
      String zipMethods = generateZipMethods(complexity);
      result.add(
        xJob
          .subst("DOC_TOP", "")
          .subst("X", complexity.prefix)
          .subst("EXTENDS", _extends)
          .substIndent("ZIP_METHODS", zipMethods)
          .withFileName(complexity.prefix + "Job.scala")
      );
      Template xAlgorithm = 
        new Template("main/codegen-scala/scavenger/XAlgorithm.scala");
      result.add(
        xAlgorithm
          .subst("x", "BLOCKED" + complexity.prefix)
          .withFileName(complexity.prefix + "Algorithm.scala")
      );
    }
    return result;
  }

  /**
   * Finds the next higher complexity for `c`, and returns the prefix of
   * the found complexity glued to the specified suffix.
   */
  private static String generateExtends(Complexity c, String suffix) {
    Complexity candidate = null;
    for (Complexity potentialParent : Complexity.values()) {
      if (potentialParent.index >= c.index) {
        if (candidate == null || potentialParent.index < candidate.index) {
          candidate = potentialParent;
        }
      }
    }
    return candidate.prefix + suffix;
  }
 
  /**
   * Generates bunch of `zip` methods for `XJob`.
   * We need multiple methods so that the compile types are always as
   * precise as possible.
   * Inside of each method, we need dispatching by runtime type, to 
   * ensure that the runtime type is always as precise as possible, even
   * if the compile-time type somehow got lost.
   */
  public static String generateZipMethods(Complexity c) {
    StringBuilder bldr = new StringBuilder();
    for (Complexity other: Complexity.values()) {
      if (other.index >= c.index) {
        bldr.append("// olololo " + other + "\n");
      }
    }
    return bldr.toString();
  }

  public static void main(String[] args) {
    out.println("Code generation runs");
    String pathToSourceManaged = args[0];
    try {
      for (Template sd: generateCode()) {
        out.println("Generating " + sd.relativeDirname + "/" + sd.fileName);
        sd.saveTo(pathToSourceManaged);
      }
    } catch (IOException e) {
      out.println("Exception occured during source code generation: ");
      e.printStackTrace();
      out.println("Source code generation failed. Exit.");
      out.println("[This is clearly a bug in the build scripts]");
      System.exit(1);
    }
  }
}