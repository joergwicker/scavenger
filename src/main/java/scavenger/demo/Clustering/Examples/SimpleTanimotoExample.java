package scavenger.demo.clustering.example;

import scavenger.demo.clustering.distance.*;
import scavenger.demo.clustering.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashMap;
import java.net.URL;
import java.util.Scanner;
import java.io.IOException;
import java.util.BitSet;

/**
 * A basic example of using Tanimoto distance, when performing Diana clustering.
 *
 */
class SimpleTanimotoExample implements java.io.Serializable
{   
    public SimpleTanimotoExample()
    {
        // 1. Create distance measure
        DistanceMeasure<BitSet> distanceMeasure = new Tanimoto();
        
        // 2. Load data
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
        
        BitSet bits3 = new BitSet(7);
        bits3.set(1);
        bits3.set(3);
        bits3.set(2);
        
        BitSet bits4 = new BitSet(7);
        bits4.set(1);
        bits4.set(2);
        bits4.set(4);
        bits4.set(5);
        
        BitSet bits5 = new BitSet(7);
        
        BitSet bits6 = new BitSet(7);
        bits6.set(0);
        bits6.set(1);
        bits6.set(2);
        bits6.set(3);
        bits6.set(4);
        bits6.set(5);
        bits6.set(6);    
            
        DataItem<BitSet> item1 = new DataItem<BitSet>("1", bits1);
        DataItem<BitSet> item2 = new DataItem<BitSet>("2", bits2);
        DataItem<BitSet> item3 = new DataItem<BitSet>("3", bits3);
        DataItem<BitSet> item4 = new DataItem<BitSet>("4", bits4);
        DataItem<BitSet> item5 = new DataItem<BitSet>("5", bits5);
        DataItem<BitSet> item6 = new DataItem<BitSet>("6", bits6);
        
        // 3. Create root node
        List<DataItem<BitSet>> data = new ArrayList<DataItem<BitSet>>();
        data.add(item1);
        data.add(item2);
        data.add(item3);
        data.add(item4);
        data.add(item5);
        data.add(item6);        
        
        TreeNode<BitSet> input = new TreeNode<BitSet>(data);
        
        // 4. Perform clustering
        Diana<BitSet> diana = new Diana<BitSet>(distanceMeasure); 
        TreeNode<BitSet> node = diana.runClustering(input, 3);
        diana.endClustering();
        
        // 5. Print results 
        printTree(node);         
    }
    
    /**
     *
     */
    private void printTree(TreeNode<BitSet> node)
    {
        Queue<TreeNode> queue = new LinkedList<TreeNode>();
        queue.add(node);
        while(!queue.isEmpty())
        {
            TreeNode r = queue.remove(); 
            r.print();
            if (r.getChildLeft() != null)
            {
                queue.add(r.getChildLeft());
                queue.add(r.getChildRight());
            }
        }
    }
    
    public static void main(final String[] args)
    {
        SimpleTanimotoExample s = new SimpleTanimotoExample();
    }
    
}