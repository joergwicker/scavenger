package scavenger.demo.clustering;

import scavenger.demo.clustering.distance.*;
import scavenger.demo.clustering.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.net.URL;
import java.util.Scanner;
import java.io.IOException;
import java.util.BitSet;
import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * T - The type given to all the TreeNode objects
 * Y - The type of the value to be compared be the ResultHandler 
 */
public abstract class ResultHandler<T, Y> implements java.io.Serializable
{   
    public abstract void addAttributeValue(Y value); 

    /**
     * Used by Diana clustering.
     * Clusters should be extracted and passed to handleResults(List<TreeNode<T>> nodes) 
     */
    public abstract void handleResults(TreeNode<T> node);
    
    /**
     * Used by bottom up clustering.
     * Clusters should be extracted and passed to handleResults(List<TreeNode<T>> nodes) 
     */
    public abstract void handleResults(TreeNodeList<T> node);
    
    /**
     * @param nodes List of the clusters to be evaluated 
     */
    public abstract void handleResults(List<TreeNode<T>> nodes);
    
}