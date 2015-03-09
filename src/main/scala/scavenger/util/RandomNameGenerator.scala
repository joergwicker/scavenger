package scavenger.util

/** 
 * A bull****-generator that generates random
 * names for worker nodes. It's slightly more
 * easier than referencing them by numbers when
 * debugging.
 */
object RandomNameGenerator {

  // ensures that there are no two actors with same name on same JVM
  private var counter = 0; 

  private val emotions = Array(
    "agreeable",
    "brave",
    "calm",
    "eager",
    "faithful",
    "gentle",
    "happy",
    "jolly",
    "kind",
    "lively",
    "nice",
    "obedient",
    "proud",
    "relieved",
    "silly",
    "thankful",
    "victorious",
    "witty",
    "zealous",
    "bored",
    "enraged",
    "grumpy",
    "thoughtful",
    "enthusiastic"
  )

  private val condition = Array(
    "alive",
    "careful",
    "clever",
    "dead",
    "easy",
    "famous",
    "gifted",
    "helpful",
    "important",
    "inexpensive",
    "odd",
    "powerful",
    "rich",
    "shy",
    "tender",
    "uninterested",
    "vast",
    "wrong",
    "brutal",
    "merciless",
    "megalomaniac",
    "generous",
    "sensitive",
    "insidious",
    "poor",
    "strong"
  )

  // I didn't smoke anything, I just copied it
  // from a website of some weird guy from Hong-Kong
  private val alienSpecies = Array(
    "ARCTURIAN",
    "AGHARIAN",
    "ALPHA-DRACONIAN",
    "ALPHA-CENTAURIAN",
    "ALTAIRIAN",
    "AMPHIBIAN",
    "ANAKIM",
    "ANTARCTICAN",
    "ATLANS",
    "ASHTAR",
    "BERNARIAN",
    "BOOTEAN",
    "CETIAN",
    "CHAMELEON",
    "DAL",
    "DWARF",
    "EVA-BORG",
    "GIZAN",
    "GRAIL",
    "GREY",
    "GYPSIE",
    "HAV-MUSUV",
    "HU-BRID",
    "HYADEAN",
    "HYBRID",
    "IGUANOID",
    "IKEL",
    "SATYR",
    "INSIDER",
    "JANOSIAN",
    "KORENDIAN",
    "LEVIATHAN",
    "LYRAN",
    "MARTIAN",
    "MIB",
    "MOON-EYE",
    "MOTHMAN",
    "NAGA",
    "ORION",
    "PHOENICIAN",
    "PLEIADEAN",
    "PROCYONIAN",
    "RA-AN",
    "RE-BRID",
    "RETICULAN",
    "SASQUATCH",
    "SERPENT",
    "SIRIAN",
    "SOLARIAN",
    "SYNTHETIC",
    "TELOSIAN",
    "TERO",
    "ULTERRAN",
    "UMMITE",
    "VEGAN",
    "VENUSIAN",
    "ZETA-RETICULUM"
  ).map{_.toLowerCase}
  
  private val rnd = new scala.util.Random

  def randomName: String = {
    val e = rnd.nextInt(emotions.size)
    val c = rnd.nextInt(condition.size)
    val a = rnd.nextInt(alienSpecies.size)
    val name = 
      emotions(e) + "_" + 
      condition(c) + "_" + 
      alienSpecies(a) + "_" + 
      counter
    counter += 1
    name
  }

  def main(args: Array[String]): Unit = {
    for (i <- 0 to 100) {
      println(randomName)
    }
  }
}
