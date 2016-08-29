package hotswapWorker

import java.io.{File, PrintWriter}
import scala.sys.process._
import scala.language.postfixOps

/** Can create and build little java/scala projects in the `/tmp` directory.
  *
  * This allows us to build multiple versions of classes during the same test, 
  * which in turn provides meaningful test cases for our
  * `IsolatingClassLoader` and `IsolatingObjectInputStream`.
  *
  * @since 2.2
  * @author Andrey Tyukin
  */
trait TmpCompilation {

  /* LINUX: this seems very linux / unix specific. No idea what happens on
   * a windows if you try to run that...
   */
  def tmpDir = "/tmp/scavenger_tests_reloadableCode"
  def tmpDirUrl = new File(tmpDir).toURI.toURL

  /** Creates a temporary directory, creates the files (with specified names 
    * and content), invokes the specified compiler.
    *
    * Classpath of the current program is used as `-classpath` option 
    * for the compiler.
    */
  def compileTemporarySourceFiles(
    compiler: String,
    fileNamesAndSources: List[(String, String)]
  ): Unit = {
    if ((new File(tmpDir)).exists) {
      require( 
        (("rm -rf " + tmpDir) !) == 0,
        "could not remove temp dir"
      )
    }
    require( 
      (("mkdir " + tmpDir) !) == 0,
      "could not create temp dir"
    )
    var compilationCommand = 
      compiler + " -classpath " + 
      System.getProperty("java.class.path") + " "

    for ((name, content) <- fileNamesAndSources) {
      val fullName = tmpDir + "/" + name
      val f = new File(fullName)
      f.createNewFile()
      val pw = new java.io.PrintWriter(f)
      pw.println(content)
      pw.close

      compilationCommand += (fullName + " ")
    }

    assert((compilationCommand !) == 0)
  }

  /** Executes a class in the `/tmp` directory with `java` or `scala` command. 
    * Uses classpath of this testrunner as the `-classpath` option.
    */
  def runTemporaryClassFile(
    runner: String,
    classname: String
  ): Unit = {
    val cmd = (
      runner + " -classpath " + 
        System.getProperty("java.class.path") + 
        ":" + tmpDir + 
        " " + 
      classname
    )
    assert((cmd !) == 0)
  }

  /** Creates a temporary directory and a file with the source code, 
    * then compiles it with the current class path.
    */
  def compileTemporarySourceFile(
    compiler: String,
    fileName: String,
    source: String
  ): Unit = {
    compileTemporarySourceFiles(compiler, List((fileName, source)))
  }

  /** Generates a `ReloadableTestDummy`-implementation in the temporary 
    * directory, compiles it using the current class path.
    *
    */
  def compileReloadableDummyImpl(message: String): Unit = {
    compileTemporarySourceFile(
      "javac",
      "ReloadableDummyImpl.java",
      """
        import hotswapWorker.ReloadableTestDummy; 
        public class ReloadableDummyImpl implements ReloadableTestDummy {
          public String saySomething() {
            return "%s";
          }
        }
      """.format(message)
    )
  }
}