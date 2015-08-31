package scavenger.demo.clustering;
import scavenger.demo.clustering.distance.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Comparator;

/**
 * A node within the cluster hierarchy.
 */
public class TreeNodeComparator<T> implements Comparator<TreeNode<T>>
{
     public int compare(TreeNode<T> node1, TreeNode<T> node2)
     {
        if (node1.getError() < node2.getError()) return -1;
        if (node1.getError() > node2.getError()) return 1;
        return 0;
     } 
}