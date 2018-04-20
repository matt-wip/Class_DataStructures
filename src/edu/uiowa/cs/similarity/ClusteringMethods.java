
package edu.uiowa.cs.similarity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import javafx.util.Pair;
    
    /*
    Next, we want to find groups of words that are similar to each other. 
    The problem of clustering is to partition n points (in our case, a point is a semantic
    descriptor vector) into clusters of similar points. In k-means clustering, 
    given a desired number of clusters, k, we find k cluster means (a center point) 
    and assign each of the n points to the nearest mean. 
    
    In general k-means clustering tries to figure out what the optimal choice of the 
    cluster means is ("optimal" here means that the distance of each point from the mean 
    is minimized). The kmeans algorithm approximates a good set of means using iterative
    refinement. We start with random means, and each iteration moves the means to new
    places. We can stop when we have an iteration where no points switch between clusters 
    or after a fixed number of iterations.
    */
    
    // VARIABLE DEFINITIONS
    // clusters: group of semantic vectors
    // n : number of points --
    //    -- A key/unique word in uniqueMatrix- semantic vector
    // k : number of clusters, number of means
    // iter : number of iterations of refinement
    
    // DETAILS
    // For clusters: LinkedList or array of hashMap<String,Double> of size() k
    //          - each index/entry represents a cluster, so k clusters
    //          - hashsets are a collection of strings representing keys in uniqueMatrix
    //              -a.k.a hashsets contain points!
    //          - essentially, each key in uniqueMatrix is assigned to a cluster
    //      IMPORTANT: Data structure depends on how we go about deciding cluster choice
    //          Store Double as distance to assigned cluster -> will be used in part 6 and 7

    //  For means[]: LinkedList or array of size() k.
    //          - Contains HashMap<String,Double> of N keys max -> same as uniqueMatrix keys
    //          - would be best to not include keys with 0 value...
    //          - used to represent centroid/average of points in a cluster... we don't
    //              know what/how many keys are in the cluster points.
    
    //   PONDER POINTS
    //      DOES ORDER MATTER?!? 
    //          If we are just working with distances.... no, but for centroid???? it's fine
    //          -- could convert uniqueMatrix to have keys of ints instead of strings??? nah
    //      HOW CAN WE CALCULATE CENTROID FOR A SET OF POINTS/SEMANTIC VECTORS?
    //          yup, we can calculate r at the beginning as a constant (cuz uniqueMatrix
    //          key count does not change)
    //      DO WE RESET CLUSTERS EACH TIME, OR MOVE THEM? (each iteration)
    //          It would be easier to clear clusters, but would it be more efficient??? hmmm
    //      POINTS-> DO WE STORE AS STRING, OR PAIR<STRING,DOUBLE>
    //                              --(DOUBLE is distance to current cluster mean)
    //          Depends on how the point assignment works in loop, if we clear or not
    //          String for now
    
    // I'll a picture for you if ya want
//************************************************************************************


/*
 * Collection of methods to set up and analyze clusters of semantic descriptor vectors
 */
public class ClusteringMethods {
    
    // Private members
    private HashMap<String,HashMap<String,Integer>> ssMatrix;
    
    // Constructor
    public ClusteringMethods(HashMap<String,HashMap<String,Integer>> matrix){
        this.ssMatrix = matrix;        
    }
    
    // Special Top J formula for clusters -> cluster results contain distance 
    // to cluster mean, so no further calculations are necessary for top-J, 
    // just filter the lowest values.
    // Returns list of words closest to mean starting at the closest.
    public LinkedList<Pair<String,Double>> clusterTopJ(HashMap<String,Double> cluster, int topJcount){
        LinkedList<Pair<String,Double>> result = new LinkedList<>();
        if(topJcount < 1) return result; // check for valid number
        Comparator<Pair<String,Double>> comp = new pairComp();
        
        for(Entry<String,Double> p : cluster.entrySet()){
            Pair<String,Double> pPair = new Pair<String,Double>(p.getKey(),p.getValue());
            result.addFirst(pPair); // Add to first (O(1))
            result.sort(comp);      // Sort by new entry
            if(result.size() > topJcount)   // Trim if needed
                result.removeLast();
        }
        return result;
    }
    // Compares values to produce least to greatest values
    private class pairComp implements Comparator<Pair<String,Double>>{
        @Override
        public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
           return Double.compare(o1.getValue(), o2.getValue());
        }
    }
    
    /**
     * Method to create clusters using k-means clustering
     * @param k Number of clusters and number of mean vectors
     * @param iters Number of refinements
     * @return Clusters. Each List of HashMap<String,Double>. Each list is a group, 
     * note: estimated time is (1.5 * k * iters) seconds ideally
     */
    public ArrayList<HashMap<String,Double>> generateClusters(int k, int iters){
        
        // Create lists for clusters/means and add Hashes to them
        ArrayList<HashMap<String,Double>> clusters = new ArrayList<HashMap<String,Double>>(k);
        ArrayList<HashMap<String,Double>> meanList = new ArrayList<HashMap<String,Double>>(k);
        for(int i = 0; i < k; i++){
            clusters.add(i, new HashMap<String,Double>());
            meanList.add(i, new HashMap<String,Double>());
        }
        
        // Populate means using k unique random points / semantic vectors
        randomPopulate(meanList, k);
       
        // Enter refinement loop
        for(int i = 0; i < iters; i++){
            // Clear clusters
            for (HashMap<String,Double> cluster : clusters) {
                cluster.clear();
            }
            
            // Assign points to clusters -> add every unique word to a cluster
                //note: points are keys in ssMatrix
            for (Entry<String,HashMap<String,Integer>> entryMatrix : this.ssMatrix.entrySet()) {
                double min = Double.MAX_VALUE;
                int clusterIndex = 0;
                int currIndex = 0;
                
                // Calculate euclideanDistance to each mean, get min
                for(int l = 0; l < k; l++){
                    double distance = euclideanDistance(meanList.get(l), entryMatrix.getValue());
                    if(distance < min){
                        min = distance;
                        clusterIndex = currIndex;
                    }
                    currIndex++;
                }
                
              // Add key,min to cluster
                clusters.get(clusterIndex).put(entryMatrix.getKey(), min);
            }
            
            // Calculate new means from clusters   
            // AND Output average distance for every point to it's cluster's mean
            double average = 0;
            for(int j = 0; j < k; j++){
                calculateCentroid(clusters.get(j), meanList.get(j));
                average += clusterAverage(clusters.get(j));
            }
            System.out.println("Iteration " + (i+1) + ": " + average/k);
        }
        
        // clear variables (just in case)
        for(HashMap<String,Double> i : meanList){
            i.clear();
        }
        meanList.clear();
        
        return clusters; // Return results
    }
    
    // Private Methods
    private Double euclideanDistance(HashMap<String,Double> a, HashMap<String,Integer> b){
        double sumOfU_Q2 = 0;
        double diff = 0;
        
        // Go through each key in u
       // String key = null;
        for(Entry<String,Double> entry : a.entrySet()){
            String key = entry.getKey();
            diff = entry.getValue();    // all cases of bh and not qh (bh - 0)^2
            if(b.containsKey(key)){     // all cases of bh and qh (bh - qh)^2
                diff -= b.get(key);
            }
            sumOfU_Q2 += Math.pow(diff, 2);
        }
        
        for(Entry<String,Integer> entry : b.entrySet()){     // all cases of not bh and qh (0 - qh)^2
            if(!a.containsKey(entry.getKey()))
                 sumOfU_Q2 += Math.pow(entry.getValue(),2);
        }

        return Math.sqrt(sumOfU_Q2);
    } 

    private void calculateCentroid(HashMap<String,Double> cluster, HashMap<String,Double> mean) {   
        // Clear means
        mean.clear();
        
        // go through each word in cluster, get semantic vector from ssMatrix
        for(String sKey : cluster.keySet()){
            // go through each semantic vector adding value to mean
            HashMap<String, Integer> semanticVector = ssMatrix.get(sKey);
            for(String sWord : semanticVector.keySet()){
                if(mean.containsKey(sWord)){
                    mean.put(sWord, mean.get(sWord) + semanticVector.get(sWord));
                }
                else{
                    mean.put(sWord, (double)semanticVector.get(sWord));
                }
            }
        }
            
        // go through mean and divide by r - cluster size
        int size = ssMatrix.size();
        for(String sMeanKey : mean.keySet()){
            mean.put(sMeanKey, mean.get(sMeanKey)/size);
        }
    }
    
    // PART 6
    private double clusterAverage(HashMap<String,Double> hash)
    {    
        if(hash.size()==0) return 0.0;
        
        double sum = 0;
        for (double val : hash.values()) {
            sum += val;
        }
        
        return sum/hash.size();
    }
    
    /**
     * Assigns random vectors from SS Matrix to means
     * @param means 
     */
    private void randomPopulate(ArrayList<HashMap<String,Double>> means, int size){
        int max = this.ssMatrix.size();
        Object[] ssKeys = ssMatrix.keySet().toArray();
        
        // Testing/for fun:
        System.out.print("Random keys for mean generation: "); 
        
        // Fill means with k semantic vectors
        for(int k = 0; k < size; k++){
            int rand = (int)(Math.random()*max);
            
            // Testing/for fun:
            System.out.print(ssKeys[rand]+ ",");
            
            // Get matrix to convert into means
            HashMap<String,Integer> ssMatrixEntry = this.ssMatrix.get(ssKeys[rand]);
            HashMap<String, Double> meanEntry = new HashMap<String,Double>();
            // iterate through random semantic vector in ssMatrix, convert and add to means vector
            for(String ssMatrixKey : ssMatrixEntry.keySet()){
                meanEntry.put(ssMatrixKey, (double)ssMatrixEntry.get(ssMatrixKey));
            }            
            // Add converted semantic vector to means
            means.add(k, meanEntry);
        } 
            // Testing/for fun:
            System.out.println("\n");
    }
}
