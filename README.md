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

The following command can be used to clean compile and run scavenger :

```
sbt clean compile run
```

Create and run JAR file
-----------------------

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

For the basic demo run :

```
java -cp target/scala-2.10/scavenger-assembly-2.1.jar scavenger.demo.Demo
```

Examples
====

We have a seperate demo git repository with a few examples and more documentation at https://github.com/joergwicker/scavenger_demo 

Cite
====

Examples
====

We have a seperate demo git repository with a few examples and more documentation at https://github.com/joergwicker/scavenger_demo 


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
series = {Lecture Notes in Computer Science},
pubstate = {published},
tppubtype = {inproceedings}
}
```

