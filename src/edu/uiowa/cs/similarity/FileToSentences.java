/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uiowa.cs.similarity;

import java.io.*;
import java.util.*;
import opennlp.tools.stemmer.PorterStemmer;


/**
 *
 * @author AshleyHoffman
 */
public class FileToSentences {
    //method: split file, take in string or File as parameter.
    //  Return a list of lists Big list= everything, small list = sentences
    //scan.useDelimiter("[.?!]");
    public List<String> ToSentences(String filename) throws FileNotFoundException {
        //Initialize list
        List<String> sentencesWithPunct = new LinkedList<>();
        List<String> sentences = new LinkedList<>();
        
        //Iterate through file contents
        Scanner scan = new Scanner(new File(filename));
        scan.useDelimiter("[.?!]");
        while(scan.hasNext()) {
            sentencesWithPunct.add(scan.next());
        }
        
        // Punctuation removal code found from...
        // source: https://mrtextminer.wordpress.com/2008/02/14/java-regular-expression-replace-some-punctuations/
        for (String sentence : sentencesWithPunct) {
            String newSentence = sentence.replaceAll("[\\p{Punct}&&[^-']]|[-]|[\"]","").replaceAll("--","").replaceAll("\r\n|\r|\n"," ").trim();
            if (!newSentence.isEmpty()) {
                sentences.add(newSentence);
            }
        }
        
        return sentences;
    }
    
    // Note; can work with individual words or sentences
    public void ListToLower(List<String> upper) {
        ListIterator<String> it = upper.listIterator();
        while(it.hasNext()){
            it.set(it.next().toLowerCase());
        }
    }

    /*
    // wordList-> list of individual words, no sentences!
    // Sentences would have to be split up before calling this method
    public void RemoveRoots(List<String> wordList){
        PorterStemmer stemThingy = new PorterStemmer();
        ListIterator<String> it = wordList.listIterator();
        while(it.hasNext()){
            it.set(stemThingy.stem(it.next()));
        }
    }*/
    
    public static String RemoveRoots(String word){
        PorterStemmer stemThingy = new PorterStemmer();
        return stemThingy.stem(word);
    }
    
}
