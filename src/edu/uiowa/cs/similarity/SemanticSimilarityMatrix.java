/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uiowa.cs.similarity;

import java.util.*;
import javafx.util.Pair;

/**
 * Class for creating semantic descriptor vectors and analyzing them
 * @author Matt
 */
public class SemanticSimilarityMatrix {
    
    private HashMap<String,HashMap<String,Integer>> uniqueMatrix = new HashMap<String,HashMap<String,Integer>>() {};
    /**
     * Accessor to uniqueMatrix
     */
    public HashMap<String,HashMap<String,Integer>> getSSMatrix(){
        return uniqueMatrix;
    }
           
    /**
     * Constructor 
     * @param sentences Filtered sentences
     */
    public SemanticSimilarityMatrix(HashSet<LinkedHashSet<String>> sentences){
        //outer hash mess
        Iterator<LinkedHashSet<String>> sentIter = sentences.iterator();
        while (sentIter.hasNext()) { // go through each sentence in file
            HashSet<String> singleSentence = sentIter.next();
            Iterator<String> wordIter = singleSentence.iterator(); 
            while (wordIter.hasNext()) {    // go through each word in sentence
                String word = wordIter.next();
                if(!uniqueMatrix.containsKey(word)) { // if word is unique...
                    uniqueMatrix.put(word, createSemanticVector(sentences, word)); //... create semantic vector
                }
            }
        }
    }

    private HashMap<String,Integer> createSemanticVector(HashSet<LinkedHashSet<String>> sentences, String keyWord) {
        //inner hash mess
        HashMap<String,Integer> ret = new HashMap<>();
        LinkedHashSet<String> wordsUsed = new LinkedHashSet<>();
        
        Iterator<LinkedHashSet<String>> sentIter = sentences.iterator();
        while (sentIter.hasNext()) { // go through each sentence in file
            LinkedHashSet<String> singleSentence = sentIter.next();
            if (singleSentence.contains(keyWord)) { // sentence has word to generate vectors on
                Iterator<String> wordIter = singleSentence.iterator(); 
                while (wordIter.hasNext()) { // go through each word in sentence
                    String temp = wordIter.next();
                    if (!temp.equals(keyWord) && !wordsUsed.contains(temp)) {
                        wordsUsed.add(temp);
                        int value = ret.containsKey(temp) ? ret.get(temp) : 0;
                        ret.put(temp,value+1);
                    }
                }
            }
            wordsUsed.clear();
        }
        
        return ret;
    }
    
    /**
     * Prints all words and their associated semantic matrix
     */
    public void printAll() {
        for (String key : uniqueMatrix.keySet()) {
            System.out.println(key + ": " + uniqueMatrix.get(key).toString());
        }
    }
       

    /**
     * Generates a list of common words
     * @param comparisonWord
     * @param maxCount
     * @return List in order from most similar to least similar words
     */
    public LinkedList<Pair<String,Double>> generateTopList(String comparisonWord, Integer maxCount, String method){
        // Check if comparisonWord exists in semanticMatrix
        if(!uniqueMatrix.containsKey(comparisonWord)) return null;
        // Check count
        if(maxCount < 1) return null;
        
        LinkedList<Pair<String,Double>> resultList = new LinkedList<Pair<String,Double>>();
        Comparator<Pair<String,Double>> pComp = new pairComp();

        for(String mKey : uniqueMatrix.keySet()){
            Pair<String,Double> result = this.calculateSimilarity(comparisonWord, mKey, method);
            if(result == null) continue;
              
            // Insert result and sort list
            resultList.addFirst(result); //O(1)
            resultList.sort(pComp); // high to low //fast cause < maxCount items
            
            // Trim length
            if(resultList.size() > maxCount)
                resultList.removeLast(); //O(1)
       }
        return resultList;
    }
    // Compares values to produce greatest to least values
    private class pairComp implements Comparator<Pair<String,Double>>{
        @Override
        public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
           return Double.compare(o2.getValue(), o1.getValue());
        }
    }
    
    /**
     * Calculates the similarity between two words
     * @param base Word to compare to
     * @param query Word compared to base
     * @param method of algorithm to use. from -m option (Part 4)
     * @return Query string and calculated similarity
     */
    public Pair<String,Double> calculateSimilarity(String base, String query, String method){       
        // Self similar case (ignore since result is 1.0)
        if(base.equals(query)) return null;
        
        Double similarityMeasure = 0.0;
        if(method.equals("euc"))
            similarityMeasure = algorithmEul(base,query);
        else if(method.equals("eucnorm"))
            similarityMeasure = algorithmEulNorm(base,query);
        else // default cosine
            similarityMeasure = algorithmCosine(base,query);
     
        if(similarityMeasure == null) return null;
        
        return new Pair<String,Double>(query, similarityMeasure);
    }
    
    // Algorithm for cosine similarity
    private Double algorithmCosine(String base, String query){
        // Local varsss
        HashMap<String,Integer> bh = uniqueMatrix.get(base);
        HashMap<String,Integer> qh = uniqueMatrix.get(query);
        double sumOfUV = 0;
        double sumOfU2 = 0;
        double sumOfV2 = 0;
        
        // Go through each key in u
        for(String bKey : bh.keySet()){
            sumOfU2 += Math.pow(bh.get(bKey), 2); // add to sumOfU2
            if(qh.containsKey(bKey)){  // if v contains key, multiply uv, add to sumOfUV
                sumOfUV += bh.get(bKey) * qh.get(bKey);
            }
        }
        
        // Go through each key in v
        for(Integer iQ : qh.values()){
            sumOfV2 += Math.pow(iQ,2); // add to sumOfV2
        }
        
        double sqrt = Math.sqrt(sumOfU2*sumOfV2); // denominator
        if(sqrt == 0) return null; // divide by 0 error/crash.... should not happen...

        return sumOfUV/sqrt;
    }
    
    // Algorithm for Euclidean distance
    private Double algorithmEul(String base, String query){
        HashMap<String,Integer> bh = uniqueMatrix.get(base);
        HashMap<String,Integer> qh = uniqueMatrix.get(query);
        double sumOfU_Q2 = 0;
        double diff = 0;
        
        // Go through each key in u
        for(String bKey : bh.keySet()){
            diff = bh.get(bKey);            // all cases of bh and not qh (bh - 0)
            if(qh.containsKey(bKey)){       // all cases of bh and qh (bh - qh)
                diff -= qh.get(bKey);
            }
            sumOfU_Q2 += Math.pow(diff, 2);
        }
        
        for(String qKey : qh.keySet()){
            if(!bh.containsKey(qKey))
                 sumOfU_Q2 += Math.pow(qh.get(qKey),2);
        }
        
        return -Math.sqrt(sumOfU_Q2);
    }
    
    // Algorithm for Normalized Euclidean distance
    private Double algorithmEulNorm(String base, String query){
        HashMap<String,Integer> bh = uniqueMatrix.get(base);
        HashMap<String,Integer> qh = uniqueMatrix.get(query);
        double sumNormDiff = 0;
        double magB2 = 0;
        double magQ2 = 0;
        double diff = 0;
        
        // Go through each key in u
        for(String bKey : bh.keySet()){
            magB2 += Math.pow(bh.get(bKey),2);
        }
        magB2 = Math.sqrt(magB2);
        // Go through each key in v
        for(String qKey : qh.keySet()){
            magQ2 += Math.pow(qh.get(qKey),2);
        }
        magQ2 = Math.sqrt(magQ2);
        
        // Check for 0s
        if(magB2 == 0 || magQ2 == 0) return null; // Should not happen.....
        
        for(String bKey : bh.keySet()){
            diff = bh.get(bKey)/magB2;
            if(qh.containsKey(bKey)){
                diff -= (qh.get(bKey)/magQ2);
            }
            sumNormDiff += Math.pow(diff, 2);
        }
        for(String qKey : qh.keySet()){
            if(!bh.containsKey(qKey))
                sumNormDiff += Math.pow(qh.get(qKey)/magQ2,2);
        }
       
        return -Math.sqrt(sumNormDiff);
    }
}
