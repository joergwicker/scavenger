lazy val root = (project in file(".")).
  settings(
    organization := "org.scavenger",
    name := "scavenger",
    version := "2.2.0-SNAPSHOT",
    scalaVersion := "2.11.8",

    // this is necessary in order to get the actual classpath during the
    // scalatest execution
    fork in Test := true,

    libraryDependencies ++= Seq(
      "com.typesafe.akka" % "akka-actor_2.11" % "2.4.8",
      "com.typesafe.akka" % "akka-contrib_2.11" % "2.4.9",
      "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.9",
      "org.scalatest" % "scalatest_2.11" % "3.0.0",
      "commons-io" % "commons-io" % "2.5",
      "com.typesafe" % "config" % "1.3.0"
    ),
    scalacOptions ++= Seq("-deprecation", "-feature"),
    scalacOptions in (Compile,doc) ++= Seq("-groups", "-implicits"),

    //packAutoSettings,
    javaOptions += "-Xmx8G", 

    sourceGenerators in Compile <+= sourceManaged in Compile map {
      baseDir => 
      val file = baseDir / "scavenger" / "HelloGen.scala"
      IO.write(file, """
        object HelloGen { def main(a: Array[String]) = println("hey") }
      """)
      Seq(file)
    }
  )
