package hotswapWorker
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectStreamClass

/** Subclass of `ObjectInputStream` that uses a special classloader which
  * can isolate a set of "dangerous" classes in such a way that they can
  * later be reloaded.
  *
  * Builds a list of all potentially dangerous classes that have been 
  * loaded while deserializing the data from the input stream.
  */
class IsolatingObjectInputStream(
  inputStream: InputStream,
  classloader: IsolatingClassLoader
) extends ObjectInputStream(inputStream) {

  private var _loadedDangerousClasses: List[String] = Nil

  override def resolveClass(osc: ObjectStreamClass): Class[_] = {
    val fullName = osc.getName
    if (classloader.isIsolated(fullName)) {
      _loadedDangerousClasses ::= fullName
    }
    // classloader.loadClass(fullName)
    Class.forName(fullName, false, classloader)
  }

  def loadedIsolatedClasses: Set[String] = _loadedDangerousClasses.toSet
}
