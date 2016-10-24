Scavenger
====

Machine Learning methods and algorithms are often highly modular in
the sense that they rely on a large number of subalgorithms that are
in principle interchangeable. For example, it is often possible to
use various kinds of pre- and post-processing and various base
classifiers or regressors as components of the same modular
approach. Scavenger is a framework, that allows
evaluating whole families of conceptually similar algorithms
efficiently. The algorithms are represented as compositions,
couplings and products of atomic subalgorithms. This allows partial
results to be cached and shared between different instances of a
modular algorithm, so that potentially expensive partial results
need not be recomputed multiple times. Furthermore, Scavenger
deals with issues of the parallel execution,  load balancing, and
with the backup of partial results for the case of implementation or
runtime errors.

Build
=====

Scavenger is in Maven central, include the following dependency in your pom.xml:

```
<dependency>	
    <groupId>org.kramerlab</groupId>
    <artifactId>scavenger</artifactId>
    <version>2.1</version>
</dependency>

```

if you do not use Maven in your project, you can use

```
mvn clean install
```

to build the jar files.

Run
===


Depending on your projects configuration, you can run the seed via:


```
java -cp <jar-files> scavenger.app.SeedMain
```

or, using

```
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>exec-maven-plugin</artifactId>
  <version>1.1</version>
  <executions><execution>
  </execution></executions>
</plugin>
```


```
mvn exec:java -Dexec.mainClass="scavenger.app.SeedMain" -Dconfig.file=<configFile>
```


The worker is in the class:

```
mvn exec:java -Dexec.mainClass="scavenger.app.WorkerMain" -Dakka.remote.netty.tcp.hostname=<host> -Dakka.remote.netty.tcp.port=<port> -Dconfig.file=<configFile>
```

<port> is any arbitrary port that the worker uses to communicate with the seed.

For the basic demo run:

```
mvn exec:java -Dexec.mainClass="scavenger.demo.Demo" -Dakka.remote.netty.tcp.hostname=<host> -Dakka.remote.netty.tcp.port=<port>  -Dconfig.file=<configFile>
```

Cite
====

If you use Scavenger, please cite:

```
@inproceedings{tyukin2015scavenger,
title = {Scavenger - A Framework for the Efficient Evaluation of Dynamic and Modular Algorithms},
author = { Andrey Tyukin and Stefan Kramer and Jörg Wicker},
editor = {Albert Bifet and Michael May and Bianca Zadrozny and Ricard Gavalda and Dino Pedreschi and Jaime Cardoso and Myra Spiliopoulou},
url = {http://dx.doi.org/10.1007/978-3-319-23461-8_40},
doi = {10.1007/978-3-319-23461-8_40},
isbn = {978-3-319-23460-1},
year = {2015},
date = {2015-09-07},
booktitle = {Machine Learning and Knowledge Discovery in Databases},
volume = {9286},
pages = {325-328},
publisher = {Springer International Publishing},
series = {Lecture Notes in Computer Science}
}
```

Mixed Java and Scala Classes
============================

If you have a maven project with java and scala classes, this seems to work:

```
   <dependency>
      <groupId>org.scala-lang</groupId>
      <artifactId>scala-library</artifactId>
   </dependency>

      ...

  <pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.scala-tools</groupId>
                <artifactId>maven-scala-plugin</artifactId>
                <version>2.15.2</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.5.1</version>
            </plugin>
        </plugins>
    </pluginManagement>
    <plugins>
        <plugin>
            <groupId>org.scala-tools</groupId>
            <artifactId>maven-scala-plugin</artifactId>
            <executions>
                <execution>
                    <id>scala-compile-first</id>
                    <phase>process-resources</phase>
                    <goals>
                        <goal>add-source</goal>
                        <goal>compile</goal>
                    </goals>
                </execution>
                <execution>
                    <id>scala-test-compile</id>
                    <phase>process-test-resources</phase>
                    <goals>
                        <goal>testCompile</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <executions>
                <execution>
                    <phase>compile</phase>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
```
