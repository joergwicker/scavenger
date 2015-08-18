package scavenger.demo.clustering.distance;



interface DistanceMeasure<T>
{
    
    public double getDistance(T value1, T value2);
}