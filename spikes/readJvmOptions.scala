/**
 * A tiny example that shows how to read JVM options 
 * passed as -Dkey=value to a java/scala program
 */

// Try for example:
// scala -Dfoo=42 -Dbar=blah readJvmOptions.scala
// Or, with options hidden in a separate config file:
// scala $(cat hypotheticConfigFile) readJvmOptions.scala
println("foo = " + System.getProperty("foo"))
println("bar = " + System.getProperty("bar"))
