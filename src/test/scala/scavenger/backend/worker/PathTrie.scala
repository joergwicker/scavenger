package scavenger.backend.worker

/** A "trie" that can represent families of path-like strings.
  * For example, the set {"foo/bar/baz", "foo/bar/blup"} can be
  * represented as a trie with three nodes: root "foo/bar/"
  * with two children-leafs "baz" and "blup".
  * 
  * Useful for dumping long lists of classpath elements.
  * Purely cosmetic, used only in the tests.
  * @since 2.2
  * @author Andrey Tyukin
  */
case class PathTrie(
  separator: String,
  prefix: List[String], 
  children: List[PathTrie]
) {
  private def indent(str: String): String = {
    (for (line <- str.split("\n")) yield ("    " + line)).mkString("\n")
  } 
  override def toString: String = {
    prefix.mkString("", separator, separator) + 
    (if (!children.isEmpty) "\n" else "") + 
    indent(children.mkString("\n"))
  }

  /** Recursively compresses all children. If there is only one child,
    * merges its prefix with the own prefix. 
    *
    * This results in a more compact, compressed representation 
    * with fewer nodes.
    */
  def compress: PathTrie = {
    val compressedChildren = children.map{_.compress}
    if (children.size == 1) {
      val onlyChild = compressedChildren.head
      PathTrie(separator, prefix ++ onlyChild.prefix, onlyChild.children)
    } else {
      PathTrie(separator, prefix, compressedChildren)
    }
  }
}

object PathTrie {
  /** Converts a list of strings into a compressed PathTrie
    */
  def apply(strings: List[String], separator: String): PathTrie = {
    val lists = strings.map{s => s.split(separator).toList}
    def rec(lists: List[List[String]]): List[PathTrie] = {
      if (lists.size == 1) {
        List(PathTrie(separator, lists.head, Nil))
      } else {
        (for ((p, cs) <- lists.groupBy(_.head)) yield {
          PathTrie(separator, List(p), rec(cs.map(_.tail)))
        }).toList.sortBy(_.prefix.mkString)
      }
    }
    PathTrie(separator,List(),rec(lists)).compress
  }
}

//example:
//println(PathTrie(List("foo/bar/baz", "foo/bar/blah", "foo/bar/hey/hou"), "/"))