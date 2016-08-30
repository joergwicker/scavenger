The goal of this tiny prototype is to understand whether it is possible to unload, and then to reload a class file in Java.
In principle, it should be possible using the Classloader api.

Here is what I want, more precicely:
- I want a "Main" class, which starts an iterative algorithm
- I want a class that imitates the behavior of an iterative algorithm
- I want two implementations of a "Step" of an iterative algorithm,
  one should be "bad", the other should be "good".
- I want to be able to launch "Main", then let the iterative algorithm
  run for a while, then swap out the "Step".