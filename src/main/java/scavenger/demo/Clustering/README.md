#Clustering using Scavenger#

This directory contains an example of a clustering algorithm which makes use of Scavenger. The clustering algorithm is based on Diana (DIvisive ANAlysis) clustering, but attempts to handle outliers being present in the data set.

Normally, a new cluster is started using the data item furthest from the other items in the cluster. This algorithm attempts to start the cluster using the N furthest items. (N is given as a parameter.) Using Scavenger the N options are computed in parallel. The result which contains the clusters with the smallest diameter is returned (see *scavenger.demo.clustering.errorCalculation.SimpleErrorCalculation*). 

##Examples##

- SimpleExample : Uses Euclidean distance 
- SimpleTanimotoExample
- IrisExample : Shows how to use different distance measures for the values within a data item.

##Extending##

New distance measures can be added by extending *scavenger.demo.clustering.distance.DistanceMeasure*. For an example see *scavenger.demo.clustering.distance.EuclideanDistance*.

A different error calculation can be used by extending *scavenger.demo.clustering.errorCalculation.ErrorCalculation* and passing an instance of *ErrorCalculation* to *Diana.setErrorCalculation()*.


##Chemical Data Clustering##

###Properties file###
The properties file should contain the following information:
- ARFF_FILE = /path/to/arff/file
- attribute = distanceMeasure_id 
    - attributes should be equivalent to the attributes in the ARFF_FILE.
    - distanceMeasure_id should contain the name of the distance measure being used (eg. tanimoto or euclidean).
    - Multiple attributes can be be set to the same distanceMeasure_id. These attributes will be grouped together, and will be in the order they appear in the ARFF_FILE.
- distanceMeasure_id = value
    - The value which is passed to the constructor of the distanceMeasure
- distanceMeasure_id_weight = int/double
    - The weighting given to the distance measure's results
- RUN_TIME = int
    - Seconds the clustering will run for.
- NUM_START_SPLITER_NODES = int
    - Number of different data items the new splinter will be started with.
    