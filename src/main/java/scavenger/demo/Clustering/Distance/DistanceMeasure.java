package scavenger.demo.clustering.distance;


/**
 * All distance measures should implement DistanceMeasure
 */
interface DistanceMeasure<T>
{
    
    public double getDistance(T value1, T value2);
}