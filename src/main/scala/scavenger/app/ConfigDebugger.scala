/** 
 * This program simply shows the loaded config and quits.
 */
package scavenger.app
import com.typesafe.config._

object ConfigDebugger {
  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load()
    println(conf)
  }
}
