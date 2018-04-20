/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Matt
 */
public class SimilarityAlgorithmsTests {
    
    public SimilarityAlgorithmsTests() {
    }
    
    // TODO put test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void testAlgorithms(){
        HashMap<Integer,Integer> b = new HashMap<Integer,Integer>();
        HashMap<Integer,Integer> q = new HashMap<Integer,Integer>();
        b.put(1,1);b.put(2,4);b.put(3,1);b.put(4,0);b.put(5,0);b.put(6,0);
        q.put(1,3);q.put(2,0);q.put(3,0);q.put(4,1);q.put(5,1);q.put(6,2);
        
        assertEquals(-1.2786131660, algorEucNorm(b,q), 0.000001);
        assertEquals(-5.1961524227, algorEuc(b,q), 0.000001);
                
    }
   
    public double algorEuc(HashMap<Integer,Integer> bh, HashMap<Integer,Integer> qh){ //b and q same size
        double sumOfU_Q2 = 0;
        double diff = 0;
        
              // Go through each key in u
        for(Integer bKey : bh.keySet()){
            diff = bh.get(bKey);            // all cases of bh and not qh (bh - 0)
            if(qh.containsKey(bKey)){       // all cases of bh and qh (bh - qh)
                diff -= qh.get(bKey);
            }
            sumOfU_Q2 += Math.pow(diff, 2);
        }
        
        for(Integer qKey : qh.keySet()){
            if(!bh.containsKey(qKey))
                 sumOfU_Q2 += Math.pow(qh.get(qKey),2);
        }
        
        return -Math.sqrt(sumOfU_Q2);
    }
    
    public double algorCosine(HashMap<Integer,Integer> b, HashMap<Integer,Integer> q){ //b and q same size
        double sumOfUV = 0;
        double sumOfU2 = 0;
        double sumOfV2 = 0;
        
        // Go through each key in u
        for(int i = 0; i < b.size(); i++){
            sumOfU2 += Math.pow(b.get(i), 2); // put to sumOfU2
            sumOfUV += b.get(i) * q.get(i);
        }
        
        // Go through each key in v
        for(Integer iQ : q.values()){
            sumOfV2 += Math.pow(iQ,2); // put to sumOfV2
        }
        
        double sqrt = Math.sqrt(sumOfU2*sumOfV2); // denominator
        if(sqrt == 0) return 0; // divide by 0 error/crash.... should not happen...

        return sumOfUV/sqrt;
    }
    
   
    public Double algorEucNorm(HashMap<Integer,Integer> bh, HashMap<Integer,Integer> qh){ //b and q same size
        double sumNormDiff = 0;
        double magB2 = 0;
        double magQ2 = 0;
        double diff = 0;
        
        // Go through each key in u
        for(Integer bKey : bh.keySet()){
            magB2 += Math.pow(bh.get(bKey),2);
        }
        magB2 = Math.sqrt(magB2);
        // Go through each key in v
        for(Integer qKey : qh.keySet()){
            magQ2 += Math.pow(qh.get(qKey),2);
        }
        magQ2 = Math.sqrt(magQ2);
        
        // Check for 0s
        if(magB2 == 0 || magQ2 == 0) return null; // Should not happen.....
        
        for(Integer bKey : bh.keySet()){
            diff = bh.get(bKey)/magB2;
            if(qh.containsKey(bKey)){
                diff -= (qh.get(bKey)/magQ2);
            }
            sumNormDiff += Math.pow(diff, 2);
        }
        for(Integer qKey : qh.keySet()){
            if(!bh.containsKey(qKey))
                sumNormDiff += Math.pow(qh.get(qKey)/magQ2,2);
        }
       
        return -Math.sqrt(sumNormDiff);
    }
}
