package hotswapWorker
import org.scalatest._
import java.net.URL
import java.io.File

class IsolatingClassLoaderSpec 
extends FlatSpec 
with Matchers 
with TmpCompilation {

  "TmpCompilation" should "compile test classes with the current classpath" in {
    compileReloadableDummyImpl("Hello, world!")
    
    assert(
      new File(tmpDir + "/ReloadableDummyImpl.class").exists, 
      "A ReloadableDummyImpl.class file has been created"
    )
  }

  "IsolatingClassLoader" should 
  "transform the classpath into a non-empty List of URLs" in {
    val classPathUrls = IsolatingClassLoader.getClassPathURLs
    (classPathUrls.isEmpty) should be (false)
    // info(
    //   "The current class path consists of the following URLs: \n" + 
    //   PathTrie(classPathUrls.map{_.toString}.toList, "/")
    // )
  }

  it should "add specified extra URLs to the classpath" in {
    val extra = List(
      new URL("file:///tmp/nowhere!/")
    )
    val cl = new IsolatingClassLoader(
      classOf[IsolatingClassLoaderSpec].getClassLoader,
      List("prefixDoesNotMatterNow"),
      extra
    )
    val actualURLs = cl.getURLs
    val containsAll: Boolean = extra.forall(url => actualURLs.contains(url)) 
    containsAll should be (true)
  }

  it should 
  "load non-dangerous classes from the classpath of this test-runner" in {
    val icl = new IsolatingClassLoader(
      classOf[IsolatingClassLoaderSpec].getClassLoader,
      List(),
      List()
    )
    val clazz: Class[_] = icl.loadClass("hotswapWorker.LoadMe")
    val inst = clazz.newInstance
    inst.toString should be ("I am a Java class")
  }

  it should 
  "load dangerous classes from the classpath of this test-runner" in {
    val icl = new IsolatingClassLoader(
      classOf[IsolatingClassLoaderSpec].getClassLoader,
      List("dangerous."),
      List()
    )
    val clazz: Class[_] = icl.loadClass("dangerous.LoadMe")
    val inst = clazz.newInstance
    inst.toString should be ("Grrr, I'm dangerous!")
  }

  it should "load classes from the specified extra classpath URLs" in {
    compileReloadableDummyImpl("hello")
    val extraURL = new File(tmpDir).toURI.toURL
    val icl = new IsolatingClassLoader(
      classOf[IsolatingClassLoaderSpec].getClassLoader,
      List("Reloadable"),
      List(extraURL)
    )
    // info("Extra URL: " + extraURL)
    // info("Full classpath: \n" + 
    //   PathTrie(icl.getURLs.map{_.toString}.toList, "/")
    // )
    val clazz: Class[_] = icl.loadClass("ReloadableDummyImpl")
    val inst = clazz.newInstance.asInstanceOf[ReloadableTestDummy]
    val response = inst.saySomething
    response should be ("hello")
  }

  it should "recognize by name which classes should be isolated" in {
    val examples = List(
      ("org.ok.Good", false),
      ("com.blah.Nice", false),
      ("edu.suspicious.Foo", true),
      ("com.dangerous.Plague", true),
      ("com.dangerous.Cholera", true),
      ("edu.suspicious.Junk", true),
      ("hell.lo.World", false)
    )
    val icl = new IsolatingClassLoader(
      classOf[IsolatingClassLoaderSpec].getClassLoader,
      List("edu.suspicious", "com.dangerous"),
      List()
    )

    for ((name, isIsolated) <- examples) {
      icl.isIsolated(name) shouldEqual isIsolated
    }
  }

  it should "enable us to reload a class by disposing the classloader" in {
    for (msg <- List("foo", "bar", "baz")) {
      compileReloadableDummyImpl(msg)
      val icl = new IsolatingClassLoader(
        classOf[IsolatingClassLoaderSpec].getClassLoader,
        List("Reloadable"),
        List((new File(tmpDir)).toURI.toURL)
      )
      val clazz: Class[_] = icl.loadClass("ReloadableDummyImpl")
      val inst = clazz.newInstance.asInstanceOf[ReloadableTestDummy]
      val response = inst.saySomething
      response should be (msg)
    }
  }
}