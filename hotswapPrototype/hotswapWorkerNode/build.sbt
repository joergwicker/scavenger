lazy val root = (project in file(".")).
  settings(
    organization := "org.kramerlab",
    version := "0.1",
    name := "hotswapWorker",
    scalaVersion := "2.11.8",
    scalacOptions := Seq("-deprecation", "-feature"),

    // this is necessary in order to get the actual classpath during the
    // scalatest execution
    fork in Test := true,

    libraryDependencies ++= Seq(
     "com.typesafe.akka" % "akka-actor_2.11" % "2.4.8",
     "org.scalatest" % "scalatest_2.11" % "3.0.0",
     "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.9",
     "commons-io" % "commons-io" % "2.5"
    )
  )
