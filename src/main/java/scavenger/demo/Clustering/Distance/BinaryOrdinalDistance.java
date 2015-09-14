package scavenger.demo.clustering.distance;
import scavenger.demo.clustering.*;
import java.util.BitSet;

/**
 * Used to calculate distance between two BitSets
 */
public class BinaryOrdinalDistance extends DistanceMeasure<BitSet>
{
    /**
     * 
     * @param value1
     * @param value2
     * @return distance between value1 and value2
     */
    public double calculateDistance(BitSet value1, BitSet value2)
    {
        if (value1.size() != value2.size()) 
        {
            return 0.0;
        }
        
        double cardinality1 = value1.cardinality();
        value2.xor(value1);
        double cardinality2 = value2.cardinality();
        
        return cardinality2 / cardinality1;
    }
    
    
    /**
     * For testing
     */
  /*  public static void main(final String[] args)
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
    }*/
}