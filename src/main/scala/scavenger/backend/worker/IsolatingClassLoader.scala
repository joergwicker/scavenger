package scavenger.backend.worker
import java.lang.ClassLoader
import java.net.URL
import java.net.URLClassLoader
import java.io.File

/** Loads classes in such a way that they can later be unloaded by 
  * disposing this classloader.
  *
  * This classloader subdivides all loaded classes into two sets: 
  * a distinguished set of "dangerous" classes, and all other classes.
  * When the `loadClass` method is called, this classloader defines the
  * "dangerous" classes on its own, and delegates the loadng of all other
  * classes to the parent classloader. 
  * If the bytecode of the "dangerous" classes changes, this classloader can 
  * be discarded, and the classes can then be reloaded again.
  * 
  * The criterion for deciding which classes are considered to be 
  * "dangerous" is very simple: if the list `prefixes` contains a prefix 
  * of the full class name, then the class is considered to be "dangerous".
  * For example, if we want to be able to reload 
  * classes whose full names start with "org.unstable." and "com.dangerous.",
  * we would pass these two prefixes to the constructor.
  */
class IsolatingClassLoader private[worker] (
  parent: ClassLoader,
  prefixes: List[String],
  extraUrlsToLookForDangerousClasses: List[URL]
) extends URLClassLoader(
  IsolatingClassLoader.getClassPathURLs ++ 
  extraUrlsToLookForDangerousClasses,
  parent
) {

  private var _loadedIsolatedClasses: Set[String] = Set.empty[String]

  def isIsolated(fullName: String): Boolean = {
    prefixes.exists{fullName.startsWith}
  }
 
  /** Behaves as URLClassLoader for classes that should be isolated.
    * Delegates the loading of all other classes to the parent classloader.
    */
  override def loadClass(fullName: String): Class[_] = {
    Option(findLoadedClass(fullName)).getOrElse{
      if (isIsolated(fullName)) {
        _loadedIsolatedClasses += fullName
        findClass(fullName)
      } else {
        parent.loadClass(fullName)
      }
    }
  }

  def loadedIsolatedClasses: Set[String] = _loadedIsolatedClasses
}

object IsolatingClassLoader {
  /** The usual way to instantiate an `IsolatingClassLoader`.
    */
  def apply(
    parent: ClassLoader,
    prefixes: List[String]
  ) = new IsolatingClassLoader(parent, prefixes, List())

  // Transforms the current classpath into list of URLs,
  // (suggested by Ullenboom)
  def getClassPathURLs: Array[URL] = 
    System.getProperty("java.class.path").split(java.io.File.pathSeparator).map{
      f => new File(f).toURI.toURL
    }
}
