package hotswapWorker

import org.scalatest._
import java.io._
import java.net._

/** Tests the expected behavior of an `IsolatingObjectInputStream`,
  * but also our understanding of the URLClassLoader.
  *
  * @since 2.2
  * @author Andrey Tyukin
  */
class IsolatingObjectInputStreamSpec 
extends FlatSpec
with Matchers
with MockupClient {

  // brief test for the test-framework itself
  "MockupClient" should 
  "compile and send us instances of generated classes over socket" in {
    receivingSerialized(FakeJobClass("Test", "the tester", Nil)) { 
      inputStream => /* it just shouldn't crash in unexpected ways */
    }
  }

  // testing our assumptions about the standard URLClassLoader
  "URLClassLoader with an extended classpath" should 
  "load classes compiled by MockupClient" in {
    (receivingSerialized(FakeJobClass("Foo", "bar", Nil)) {
      inputStream =>
      val cl = new URLClassLoader(
        IsolatingClassLoader.getClassPathURLs :+ 
        tmpDirUrl,
        classOf[IsolatingClassLoaderSpec].getClassLoader
      )
      cl.loadClass("Foo").newInstance.toString
    }) shouldEqual "Foo{bar}()"
  }

  // testing our assumptions about slightly modified ObjectInputStream
  "ObjectInputStream with a properly configured URLClassLoader" should
  "deserialize instances of classes compiled by MockupClient" in {
    (receivingSerialized(FakeJobClass("Foo", "bar", Nil)) {
      inputStream =>
      val cl = new URLClassLoader(
        IsolatingClassLoader.getClassPathURLs :+ tmpDirUrl,
        classOf[IsolatingClassLoaderSpec].getClassLoader
      )
      val ois = new ObjectInputStream(inputStream) {
        override def resolveClass(osc: ObjectStreamClass): Class[_] = {
          Class.forName(osc.getName, false, cl)
        }
      }
      val obj = ois.readObject
      obj.toString
    }) shouldEqual "Foo{bar}()"
  }

  // Testing our custor IsolatingObjectInputStream
  "IsolatingObjectInputStream" should 
  "be able to load a dangerous class sent by MockupClient" in {
    (receivingSerialized(FakeJobClass("DangerousFoo", "bar", Nil)) {
      inputStream =>
      val icl = new IsolatingClassLoader(
        classOf[IsolatingObjectInputStreamSpec].getClassLoader,
        List("Dangerous"),
        List(tmpDirUrl)
      )
      
      val iois = new IsolatingObjectInputStream(inputStream, icl)
      val obj = iois.readObject()
      obj.toString
      
    }) shouldEqual "DangerousFoo{bar}()"
  }

  it should "maintain a list of isolated loaded classes" in {
    val dangerousClassWithDependencies = FakeJobClass(
      "DangerousFoo",
      "foo",
      List(
        FakeJobClass(
          "DangerousBar",
          "bar",
          Nil
        ),
        FakeJobClass(
          "DangerousBaz",
          "baz",
          Nil
        )
      )
    )

    (receivingSerialized(dangerousClassWithDependencies) {
      inputStream =>
      val icl = new IsolatingClassLoader(
        classOf[IsolatingObjectInputStreamSpec].getClassLoader,
        List("Dangerous"),
        List(tmpDirUrl)
      )
      
      val iois = new IsolatingObjectInputStream(inputStream, icl)
      iois.readObject()
      iois.loadedIsolatedClasses.toSet
    }) shouldEqual (Set("DangerousFoo", "DangerousBar", "DangerousBaz"))
  }

  it should "deserialize multiple versions of the same class" in {
    for (i <- 1 to 3) {
      val dangerousWithDependency = FakeJobClass(
        "DangerousFoo",  // <--- the name remains the same
        "foo_" + i,      // <--- the implementation changes
        List(
          FakeJobClass(
            "DangerousBar",
            "bar_" + i,
            Nil
          )
        )
      )
      (receivingSerialized(dangerousWithDependency) {
        inputStream =>
        val icl = new IsolatingClassLoader(
          classOf[IsolatingObjectInputStreamSpec].getClassLoader,
          List("Dangerous"),
          List(tmpDirUrl)
        )
        val iois = new IsolatingObjectInputStream(inputStream, icl)
        val obj = iois.readObject()
        (obj.toString) shouldEqual (
          "DangerousFoo{foo_%d}(DangerousBar{bar_%d}())".format(i, i)
        )
        iois.loadedIsolatedClasses.toSet shouldEqual(
          Set("DangerousFoo", "DangerousBar")
        )
      })
    }
  }
}