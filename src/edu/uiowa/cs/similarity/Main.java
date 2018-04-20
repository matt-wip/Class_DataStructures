package edu.uiowa.cs.similarity;

import org.apache.commons.cli.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

public class Main {

    public static void main(String[] args) {
        Options options = new Options();
        options.addRequiredOption("f", "file", true, "input file to process");
        options.addOption("h", false, "print this help message");
        options.addOption("s", false, "print sentences");                   // part 1
        options.addOption("v", false, "print semantic vector");             // part 2
        options.addOption("t", true, "print n most similar words. n > 0");  // part 3
        options.addOption("m", true, "method of analyzing "
                + "similarity: euc, eucnorm, cosine");                      // part 5
        options.addOption("k", true, "clustering. format: "                 // part 6
                + "[number of clusters],[number of iterations]");                       
        options.addOption("j",true, "clustering with top j. format: "       // part 7
                + "[number of clusters],[number of iterations],[J]");  
        options.addOption("i", false, "interactive");                       // EXTRA CREDIT

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        SemanticSimilarityMatrix semanticMatrix = null;
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            new HelpFormatter().printHelp("Main", options, true);
            System.exit(1);
        }

        String filename = cmd.getOptionValue("f");
		if (!new File(filename).exists()) {
			System.err.println("file does not exist "+filename);
			System.exit(1);
		}
         
        // Part 1 Stuff
        FileToSentences converter = new FileToSentences();
        try {
            // Get sentences, remove unnecessary punctuation, and convert to lowercase
            List<String> sentences = converter.ToSentences(filename);
            converter.ListToLower(sentences);           
            
            // Convert to Sentence lists of Word lists
            HashSet<LinkedHashSet<String>> kFinalList = SentenceCleaner.ConvertToListHash(sentences);
            sentences.clear();

            // Get rid of the stop words and do the root thing
            SentenceCleaner sCleanMachine = new SentenceCleaner();
            Iterator<LinkedHashSet<String>> it = kFinalList.iterator();
            while(it.hasNext()){
                LinkedHashSet<String> temp = it.next();
                sCleanMachine.RemoveStopWords(temp);
                SentenceCleaner.RemoveRootsSentence(temp);
            }
                        
            // Create matrix for future requests
            semanticMatrix = new SemanticSimilarityMatrix(kFinalList);
            
            // Part 1: print the sentences and sentence count if prompted
            if (cmd.hasOption("s")) {
                System.out.println("Sentences:");
                System.out.println(kFinalList);
                System.out.println("Number of Sentences:");
                System.out.println(kFinalList.size());
            }          
            
            // Clear memory
            kFinalList.clear();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
                   
        // Part 2: print all semantic sentences
        if (cmd.hasOption("v")) {
            semanticMatrix.printAll();
        }

        // Part 3: print top n similar words
        if(cmd.hasOption("t")){
            // Get t params and check for valid count
            String[] tArgs = cmd.getOptionValue("t").split(",");
            if(tArgs.length != 2){ // too many or too few arguments....
               System.err.println("-t option must have two arguments. Ex: -t cat,6");
               System.err.println("Similar word generation failed");
            }
            String query = FileToSentences.RemoveRoots(tArgs[0].trim().toLowerCase());

            // Parse int
            int maxCount = 0;
            try{maxCount = Integer.parseInt(tArgs[1]);}
            catch(NumberFormatException e){
               System.err.println("Check -t option for number validity." + e.getMessage());
            }
            
            // Part 5: Get algorithm option if applicable
           String algorithmOpt = "";
           if(cmd.hasOption("m")) algorithmOpt = cmd.getOptionValue("m");
            
            LinkedList<Pair<String,Double>> numList = semanticMatrix.generateTopList(query, maxCount, algorithmOpt);
            if(numList == null) System.out.println("Cannot compute top-"+maxCount+" similar words to " + tArgs[0]);
            else{ //print out list
                System.out.println("\n"+maxCount+" most similar words to " + tArgs[0]);
                for(Pair<String,Double> result : numList){
                    System.out.println(result.getKey() + " : " + result.getValue());
                }
            }
        }
        
        // Part 6
        if(cmd.hasOption("k")){
            // Get k params and check for valid count
            String[] kArgs = cmd.getOptionValue("k").split(",");
            if(kArgs.length != 2){ // too many or too few arguments....
               System.err.println("-k option must have two arguments. Ex: -k 4,6");
               System.err.println("Cluster generation failed");
            }
            int k = 0, iter = 0;
            try{
                k = Integer.parseInt(kArgs[0]);
                iter = Integer.parseInt(kArgs[1]);
            }
            catch(NumberFormatException e){
                System.out.println("Check -k option for numbers");
            }
            
            System.out.println("Generating clusters for -k option");
            System.out.println("   Number of clusters: " + k);
            System.out.println("   Number of iterations: " + iter);
            
            // NOTE: might run different each time due to random selection
            // of mean points at start and number of clusters
            ClusteringMethods cm = new ClusteringMethods(semanticMatrix.getSSMatrix());
            ArrayList<HashMap<String,Double>> list = cm.generateClusters(k, iter);
            for(int index = 0; index < list.size(); index++){
                System.out.println("Cluster " + (index + 1));
                for(String s : list.get(index).keySet()){
                    System.out.print(s + ", ");
                }
                System.out.println("");
            }
        }
        
        // Part 7
        if(cmd.hasOption("j")){
            // Get k params and check for valid count
            String[] jArgs = cmd.getOptionValue("j").split(",");
            if(jArgs.length != 3){ // too many or too few arguments....
               System.err.println("-j option must have three arguments. Ex: -j 4,6,5");
               System.err.println("Cluster generation failed");
            }
            int k = 0, iter = 0, j = 0;
            try{
                k = Integer.parseInt(jArgs[0]);
                iter = Integer.parseInt(jArgs[1]);
                j = Integer.parseInt(jArgs[2]);
            }
            catch(NumberFormatException e){
                System.out.println("Check -j option for numbers only");
            }
            
            System.out.println("Generating clusters for -j option");
            System.out.println("   Number of clusters: " + k);
            System.out.println("   Number of iterations: " + iter);
            System.out.println("   Number for Top J entries: " + j);
            
            // NOTE: might run different each time due to random selection
            // of mean points at start and number of clusters
            ClusteringMethods cm = new ClusteringMethods(semanticMatrix.getSSMatrix());
            ArrayList<HashMap<String,Double>> list = cm.generateClusters(k, iter);
            
            for(int index = 0; index < list.size(); index++){
                System.out.println("Cluster " + (index + 1) + " Top J results");
                System.out.print("\t--");
                for(Pair<String,Double> p : cm.clusterTopJ(list.get(index), j)){
                    System.out.print(p.getKey()/* + ":" + p.getValue().toString().substring(0,4)*/ + ",  ");
                }
                System.out.println("");
            }
        }
        
        // Extra credit
        if (cmd.hasOption("i")) {
            boolean exitProgram = false;
            String similarityAlgorithm = "cosine";  // default similarity function is cosine
            while (!exitProgram) {
                System.out.println("");
                System.out.println("Command options:");
                System.out.println("topj [word] [integer for j]");
                System.out.println("kmeans [integer for k] [integer for number of iterations]");
                System.out.println("similarity [word 1] [word 2]");
                System.out.println("setfunction [cosine OR euc OR eucnorm]");
                System.out.println("quit");
                System.out.println("Please enter a command:");
                Scanner input = new Scanner(System.in);
                String command = input.next();
                // follow command
                switch (command) {
                    case "topj":
                        // check topj inputs
                        if (!input.hasNext()) {
                            System.out.println("Invalid topj input, try again");
                            break;
                        }
                        String topWord = input.next();
                        if (!input.hasNextInt()) {
                            System.out.println("Invalid topj input, try again");
                            break;
                        }
                        int j = input.nextInt();
                        
                        // do the top j
                        topWord = FileToSentences.RemoveRoots(topWord.toLowerCase());
                        LinkedList<Pair<String,Double>> numList = semanticMatrix.generateTopList(topWord, j, similarityAlgorithm);
                        if(numList == null) System.out.println("Cannot compute top-"+j+" similar words to " + topWord);
                        else{ //print out list
                            System.out.println("\n"+j+" most similar words to " + topWord);
                            for(Pair<String,Double> result : numList){
                                System.out.println(result.getKey() + " : " + result.getValue());
                            }
                        }
                        break;
                    case "kmeans":
                        // check kmeans inputs
                        if (!input.hasNextInt()) {
                            System.out.println("Invalid kmeans input, try again");
                            break;
                        }
                        int k = input.nextInt();
                        if (!input.hasNextInt()) {
                            System.out.println("Invalid kmeans input, try again");
                            break;
                        }
                        int iter = input.nextInt();
                        
                        // do the kmeans
                        ClusteringMethods cm = new ClusteringMethods(semanticMatrix.getSSMatrix());
                        ArrayList<HashMap<String,Double>> list = cm.generateClusters(k, iter);
                        for(int index = 0; index < list.size(); index++){
                            System.out.println("Cluster " + (index + 1));
                            for(String s : list.get(index).keySet()){
                                System.out.print(s + ", ");
                            }
                            System.out.println("");
                        }
                        break;
                    case "similarity":
                        // check similarity input
                        if (!input.hasNext()) {
                            System.out.println("Invalid similarity input, try again");
                            break;
                        }
                        String firstWord = input.next();
                        if (!input.hasNext()) {
                            System.out.println("Invalid similarity input, try again");
                            break;
                        }
                        String secondWord = input.next();
                        
                        // calculate the similarity
                        firstWord = FileToSentences.RemoveRoots(firstWord.toLowerCase());
                        secondWord = FileToSentences.RemoveRoots(secondWord.toLowerCase());
                        if (!semanticMatrix.getSSMatrix().containsKey(firstWord) || !semanticMatrix.getSSMatrix().containsKey(secondWord)) {
                            System.out.println("Cannot compute similarity between " + firstWord + " and " + secondWord);
                            break;
                        }
                        Pair<String,Double> similar = semanticMatrix.calculateSimilarity(firstWord,secondWord,similarityAlgorithm);
                        if (similar != null) {
                            System.out.println("Similarity between " + firstWord + " and " + secondWord + " using " + similarityAlgorithm + " similarity is:");
                            System.out.println(similar.getValue());
                        }
                        break;
                    case "setfunction":
                        // check setfunction input
                        if (!input.hasNext()) {
                            System.out.println("Invalid setfunction input, try again");
                            break;
                        }
                        String temp = input.next();
                        if (!temp.equals("cosine") && !temp.equals("euc") && !temp.equals("eucnorm")) {
                            System.out.println("Invalid setfunction input, try again");
                            break;
                        }
                        similarityAlgorithm = temp;
                        System.out.println("The active similarity function is: " + similarityAlgorithm);
                        break;
                    case "quit":
                        exitProgram = true;
                        System.out.println("bye");
                        break;
                    default:
                        System.out.println("Did not recognize command. Try again.");
                        break;
                }
            }
        }
        
        // Print help
        if (cmd.hasOption("h")) {
            HelpFormatter helpf = new HelpFormatter();
            helpf.printHelp("Main", options, true);
            System.exit(0);
        }
    }
}
