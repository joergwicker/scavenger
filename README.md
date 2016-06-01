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
mvn exec:exec -Dexec.mainClass="scavenger.app.SeedMain" -Dconfig.file=<configFile>
```


The worker is in the class:

```
mvn exec:exec -Dexec.mainClass="scavenger.app.WorkerMain" -Dakka.remote.netty.tcp.hostname=<host> -Dakka.remote.netty.tcp.port=<port> -Dconfig.file=<configFile>
```

For the basic demo run :

```
mvn exec:exec -Dexec.mainClass="scavenger.demo.Demo" -Dakka.remote.netty.tcp.hostname=<host> -Dakka.remote.netty.tcp.port=<port>  -Dconfig.file=<configFile>
```

Examples
====

We have a seperate demo git repository with a few examples at https://github.com/joergwicker/scavenger_demo (note that the README file uses sbt, but Scavenger uses Maven).


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

