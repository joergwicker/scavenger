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
    - required
- attribute = distanceMeasure_id 
    - attributes should be equivalent to the attributes in the ARFF_FILE.
    - distanceMeasure_id should contain the name of the distance measure being used (eg. tanimoto or euclidean).
    - Multiple attributes can be be set to the same distanceMeasure_id. These attributes will be grouped together, and will be in the order they appear in the ARFF_FILE.
- distanceMeasure_id = value
    - The value which is passed to the constructor of the distanceMeasure.
    - Currently only required for euclidean distance.
- distanceMeasure_id_weight = int/double
    - The weighting given to the distance measure's results
    - default = 1
- RUN_TIME = int
    - Seconds the clustering will run for.
    - default = 30
- NUM_START_SPLITER_NODES = int
    - Number of different data items the new splinter will be started with.
    - default = 3
- TEST_ATTRIBUTE = attribute
    - attribute should be equivalent to the attribute in the ARFF_FILE.
    - default = null
- TEST_ATTRIBUTE_VALUES = {value1,value2,value3}
    - ordered values for the TEST_ATTRIBUTE
    - default = null
- OUTPUT_SPLINTER_NUMBER = int
    - The number of times the initial cluster should be split up 
    - If -1 given, the clustering will continue until the RUN_TIME is reached and return the result
    - default = -1 
- OUTPUT_FILE = /path/to/output
    - Results will be show in terminal window and appended to the OUTPUT_FILE
    - default = null
- TRIMMED_MEAN_PERCENT = int int int
    - If one TRIMMED_MEAN_PERCENT value given then this will be used for all diameter calculations
    - A list of values can be given to allow a different value for the clusters at each split (length should be same as OUTPUT_SPLINTER_NUMBER)
    - default = 5
- SIMIPLE_ERROR_THRESHOLD = double
    - If the largest cluster diameter falls below the SIMIPLE_ERROR_THRESHOLD the clustering will stop
    - default = 0.0

