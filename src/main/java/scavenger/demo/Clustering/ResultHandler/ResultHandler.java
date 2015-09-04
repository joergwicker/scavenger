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
 * Clustering of chemical data
 */
public abstract class ResultHandler<T> implements java.io.Serializable
{   
    //public Goodness(Properties properties, final String GOODNESS_ATTRIBUTE_VALUES);
    
    
    public abstract void addAttributeValue(String value);
    
    /**
     *
     */
    public abstract void handleResults(TreeNode<T> node);
    
}