/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uiowa.cs.similarity;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import opennlp.tools.stemmer.PorterStemmer;

/**
 * Methods to remove root/stop words, and
 * @author Matt
 */
public class SentenceCleaner {
        
    private static List<String> stopWords;
    
    public SentenceCleaner() {
        // Read in stop file to stopWords
        stopWords = new LinkedList<String>();
        
        try{
            File stopFile = new File("../stopwords.txt");
            if(stopFile == null)    // try two paths if users setup is different....
                stopFile = new File("/stopwords.txt");
            
            Scanner kscanner = new Scanner(stopFile);
            while(kscanner.hasNext()){
                stopWords.add(kscanner.next());
            }
        }
        catch(FileNotFoundException e){System.out.println("Error reading in Stop Words file");}
    }
    
    public static LinkedHashSet<LinkedHashSet<String>> ConvertToListHash(List<String> SentenceList){
        LinkedHashSet<LinkedHashSet<String>> kFinal = new LinkedHashSet<LinkedHashSet<String>>();
        for(String sentence : SentenceList){
            LinkedHashSet<String> hash = new LinkedHashSet<String>(20);
            hash.addAll(Arrays.asList(sentence.split("\\s+")));
            if(!hash.isEmpty()){
                kFinal.add(hash);
            }
        }
        return kFinal;
    }
   
    // wordList-> list of individual words, no sentences!
    // Sentences would have to be split up before calling this method
    public static void RemoveRootsSentence(LinkedHashSet<String> wordList){
        PorterStemmer stemThingy = new PorterStemmer();
        String[] sArr = wordList.toArray(new String[0]);
        wordList.clear();
        
        for(String s : sArr){
            wordList.add(stemThingy.stem(s));
        }
    }
    
    public void RemoveStopWords(LinkedHashSet<String> wordList){
        Iterator<String> it = wordList.iterator();
        while(it.hasNext()){
            if(stopWords.contains(it.next())){
                it.remove();
            }
        }
    }
}
