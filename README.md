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

Build and Run
=============

sbt can be used to build and run scavenger. To install scavenger on OS-X run :
```
brew install sbt
```
For other operating systems see : http://www.scala-sbt.org/download.html

Build
------

The following command can be used to clean compile and run scavenger :

```
sbt clean compile run
```


Compile and run
-----------------

In order to run scavenger on mogon a jar file is required. 

To create a jar file run :

```
sbt assembly
```

To run a seed from the jar run :

```
java -cp target/scala-2.10/scavenger-assembly-2.1.jar scavenger.app.SeedMain
```

To run a worker from the jar run :

```
java -cp target/scala-2.10/scavenger-assembly-2.1.jar scavenger.app.WorkerMain
```

For the Sudoku demo run :

```
java -cp target/scala-2.10/scavenger-assembly-2.1.jar scavenger.demo.Sudoku
```

For an example of running scavenger on mogon see : scavenger/mogonJobScripts/Sudoku
