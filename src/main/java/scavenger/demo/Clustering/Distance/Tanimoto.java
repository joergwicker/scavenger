package scavenger.demo.clustering.distance;

import java.util.BitSet;

class Tanimoto implements DistanceMeasure<BitSet>
{
    // http://showme.physics.drexel.edu/usefulchem/Software/Drexel/Cheminformatics/Java/cdk/src/org/openscience/cdk/similarity/Tanimoto.java
    public double getDistance(BitSet value1, BitSet value2)
    {
        if (value1.size() != value2.size()) 
        {
            return 0.0;
        }
        
        double cardinality1 = value1.cardinality();
        double cardinality2 = value2.cardinality();
        
        BitSet bitSetAnd = (BitSet)value1.clone();
        bitSetAnd.and(value2);
        double andCardinality = bitSetAnd.cardinality();
        return (andCardinality / (cardinality1 + cardinality2 - andCardinality));        
    }
    
    
    
    public static void main(final String[] args)
    {
        BitSet bits1 = new BitSet(7);
        bits1.set(0);
        bits1.set(2);
        bits1.set(3);
        bits1.set(4);
        bits1.set(5);
        BitSet bits2 = new BitSet(7);
        bits2.set(0);
        bits2.set(3);
        bits2.set(5);
        
        Tanimoto t = new Tanimoto();
        System.out.println(t.getDistance(bits1, bits2));
    }
}