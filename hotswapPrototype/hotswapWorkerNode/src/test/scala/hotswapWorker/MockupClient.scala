package hotswapWorker

import java.net._
import java.io._
import scala.concurrent.Await
import scala.concurrent.blocking
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

trait SourceCodeGenerator {
  def fileName: String
  def fullName: String
  def sourceCode: String
  def allDependencies: Set[SourceCodeGenerator]
  def instantiationCode: String
}

case class SingleJavaClass(val fullName: String, val sourceCode: String)
extends SourceCodeGenerator {
  def fileName = fullName + ".java"
  def allDependencies = Set(this)
  def instantiationCode = "new %s()".format(fullName)
}

/** Describes the source code of an automatically generated java class.
  * 
  * This java class has a whole 
  * tree of dependencies, which gives more interesting test cases for
  * the `IsolatedClassLoader` and `IsolatedObjectInputStream`.
  */
case class FakeJobClass(
  val clazzname: String,
  val reloadablePiece: String,
  val dependencies: List[FakeJobClass]
) extends SourceCodeGenerator {
  override def toString = clazzname + dependencies.mkString("(", ",", ")")
  def fullName: String = clazzname
  def fileName: String = clazzname + ".java"
  def sourceCode: String = {
    val skeleton = """
    import java.io.Serializable;

    public class <CLASS_NAME> 
    implements Serializable {
      <MEMBERS>
      public <CLASS_NAME>(<CONSTRUCTOR_ARGS>) {
        <CONSTRUCTOR_IMPL>
      }
      @Override
      public String toString() {
        String res = "<CLASS_NAME>" + "{<RELOADABLE_PIECE>}" + "(";
          <TO_STRING_IMPL>
        if (res.charAt(res.length() - 1) == ',') {
          res = res.substring(0, res.length() - 1);
        }
        res = res + ")";
        return res;
      }
    }
    """
    val members = (
      for (dep <- dependencies) 
      yield ("private  %s %s;".format(dep.clazzname, dep.clazzname.toLowerCase))
    ).mkString("\n")
    val constructor_args = (
      for (dep <- dependencies) 
      yield ("%s %s".format(dep.clazzname, dep.clazzname.toLowerCase))
    ).mkString(",")
    val constructor_impl = (
      for (dep <- dependencies) 
      yield {
        val lc = dep.clazzname.toLowerCase 
        "this.%s = %s;".format(lc, lc)
      }
    ).mkString("\n")
    val to_string_impl = (
      for (dep <- dependencies) 
      yield {
        val lc = dep.clazzname.toLowerCase 
        "res += %s.toString();".format(lc)
      }
    ).mkString("\n")

    skeleton.
      replaceAll("<CLASS_NAME>", clazzname).
      replaceAll("<RELOADABLE_PIECE>", reloadablePiece).
      replaceAll("<MEMBERS>", members).
      replaceAll("<CONSTRUCTOR_ARGS>", constructor_args).
      replaceAll("<CONSTRUCTOR_IMPL>", constructor_impl). 
      replaceAll("<TO_STRING_IMPL>", to_string_impl)
  }
  def instantiationCode: String = "new %s(%s)".format(
    fullName,
    dependencies.map(_.instantiationCode).mkString(",")
  )

  /** Lists all source code files on which the 
    * generated class depends (itself included).
    */
  def allDependencies: Set[SourceCodeGenerator] = {
    val res = (this :: dependencies.flatMap{_.allDependencies}).toSet
    assert(
      res.size >= 1 + dependencies.size,
      "The total number of dependencies of " + this.toString + 
      " was smaller than the number of direct dependencies. " + 
      "Ensure that the dependencies form a tree, and not some kind of " +
      "more general graph."
    )
    res
  }
}

/**
 * Generates and compiles a fake client application, which then
 * can be run in a separate JVM and send requests to this test-runner.
 *
 * Notice: starting a mock-up client and sending some serialized 
 * classes is not the point of this piece of code.
 * The point is that we can re-generate and re-compile the code of the 
 * fake client while a single test-case is running. This allows us to 
 * test whether our `IsolatingObjectInputStream` can cope with multiple 
 * versions of same class by reloading the recompiled code. Thus, 
 * we can automatically simulate the situation when the user submits a job,
 * then changes and recompiles the code, and then resubmits the job again.
 *
 * @since 2.2
 * @author Andrey Tyukin
 */
trait MockupClient extends TmpCompilation {

  val serverPort = 60321

  /** Sets up a mock-up project in `tmp`, compiles it, and
    * starts a mock-up client.
    *
    * The client instantiates a freshly compiled version of `fakeJob`,
    * then serializes this object, and sends it over socket to `serverPort`
    * on the localhost. Then the client shuts down.
    * 
    */
  def compileAndSend(scg: SourceCodeGenerator): Unit = {
    val clientCode = """
    import java.io.*;
    import java.net.*;

    public class MockupClient {
      public static void main(String[] args)
      throws Exception {
        %s x = %s;
        Socket socket = new Socket("127.0.0.1", %d);
        ObjectOutputStream oos = 
          new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(x);
        oos.flush();
        oos.close();
      }
    }""".format(
      scg.fullName,
      scg.instantiationCode,
      serverPort
    )
    
    val allSources = 
      ("MockupClient.java", clientCode) ::
      (
        for (d <- scg.allDependencies) yield (d.fileName, d.sourceCode)
      ).toList
    
    assert(
      allSources.size == scg.allDependencies.size + 1,
      "Every dependency should produce a source file, + there should be " + 
      "the mock-up client itself.\n" + 
      "#Dependencies = %d; #Sources = %d".format(
        scg.allDependencies.size, 
        allSources.size
      )
    )

    compileTemporarySourceFiles("javac", allSources)
    runTemporaryClassFile("java", "MockupClient")
  }

  /** Compiles all dependencies generated by `SourceCodeGenerator`,
    * starts a separate JVM, instantiates the class described by
    * `SourceCodeGenerator`, serializes it, sends the instance over
    * socket to this test runner. The input stream with the serialized 
    * instance is fed to `f`.
    */
  def receivingSerialized[X]
    (scg: SourceCodeGenerator)
    (f: InputStream => X): X = {
    val futureResult = Future {
      blocking {
        val serv = new ServerSocket(serverPort)
        val sock = serv.accept()
        try {
          val res = f(sock.getInputStream)
          res
        } finally {
          serv.close()
        }
      }
    }

    compileAndSend(scg)

    Await.result(futureResult, 10 seconds)
  }

  /** Compiles the code, creates an instance of the class,
    * serializes it, gives it to `f` as an input stream.
    *
    * Instantiation and serialization of the class happens on an 
    * entirely separate JVM, the serialized instance is sent over 
    * socket.
    */
  def receivingSerialized[X](
    name: String,
    code: String
  )(f: InputStream => X): X = 
    receivingSerialized(new SingleJavaClass(name, code))(f)

  /** Compiles the java code, creates an instance of the class,
    * serializes it, gives it to `f` as a byte array
    *
    * Instantiation and serialization of the class happens on an 
    * entirely separate JVM, the serialized instance is sent over 
    * socket.
    */
  def receivingBytes[X](
    name: String,
    javaCode: String
  )(f: Array[Byte] => X): X = {
    receivingSerialized(name, javaCode) { inputStream =>
      f(org.apache.commons.io.IOUtils.toByteArray(inputStream))
    }
  }
}