package edu.csulb;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

import org.junit.Test;

import modules.documents.DirectoryCorpus;
import modules.documents.DocumentCorpus;
import modules.indexes.DiskIndexWriter;
import modules.indexes.DiskPositionalIndex;
import modules.indexes.Index;
import modules.indexes.Indexer;
import modules.queries.Accumulator;
import modules.queries.BooleanQueryParser;
import modules.queries.CalculatePrecision;
import modules.queries.QueryComponent;
import modules.text.AdvancedTokenProcessor;

public class Tester {
    //@Test
    public static void main(String[] args) throws IOException {
        Index index;//Declared depending on choice
        /**************************************
        *                   MENU
        **************************************/
        Scanner scan = new Scanner(System.in);
		System.out.print("1.Build Index\n2.Query Index\n");
        int userInput = scan.nextInt();
        scan.nextLine();
        while (true){     
            switch(userInput){
                case 1:
                System.out.println("What is the path of the directory you would like to index: ");
                String s = scan.nextLine();
                System.out.println(Paths.get(s).toAbsolutePath());
                //loadDirectory
				DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(s).toAbsolutePath());
				long startTime = System.nanoTime();

				//FIX ME index = indexDiskCorpus(corpus, Paths.get(s).toAbsolutePath());
                index = Indexer.indexDiskCorpus(corpus, s);

				DiskIndexWriter dw = new DiskIndexWriter();
				//dw.setDocSize(corpus.getCorpusSize());
				dw.writeIndex(index, s);
				//dw close DB;
				long endTime = System.nanoTime();
				long totalTime = endTime - startTime;
				System.out.println("Corpus indexed in: " + totalTime / 1000000000 + " seconds");
					return;
                    //break;
                case 2:
                    System.out.println("Retrieval Modes\n1.Boolean \n2.Ranked \n3.MAP");
                    userInput = scan.nextInt();
                    scan.nextLine();
                    switch(userInput) { 
                        case 1:
                            System.out.println("Enter corpus path: ");
                            String pathName = scan.nextLine();
                            //scan.nextLine();	
                            //System.out.println(Paths.get(pathName).toAbsolutePath());
                            DocumentCorpus corpusB = DirectoryCorpus.loadTextDirectory(Paths.get(pathName).toAbsolutePath());
                            corpusB.getDocuments();
                            DiskPositionalIndex dIndex = new DiskPositionalIndex(pathName);

                            while (true) {
                                System.out.println("Enter search query: ");
                                String query = "whale"; // hard-coded search for "whale"
                                query = scan.nextLine();
                                switch(query){
                                case "q":
                                    System.out.println("Shut down...");
                                    scan.close();
                                    return;//end program 
                                case "stem":
                                    AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
                                    System.out.print("Enter word:");
                                    //ArrayList<String> word = processor.processToken(scan.next());
                                    System.out.print(processor.processToken(scan.next()));
                                    //System.out.println(word.get(1));
                                    System.out.println();
                                    scan.nextLine();
                                    break;
                        case "vocab":
                            List<String> vocabList = dIndex.getVocabulary(); //make a temp vocab list from vocab
                            if(vocabList.size() >= 1000){ //check if vocab has at least 1000 words
                                for(int i = 0; i< 1000; i++){
                                    System.out.println(vocabList.get(i)); //output the list
                                }
                            }
                            else{
                                for(int i = 0; i < vocabList.size(); i++){
                                    System.out.println(vocabList.get(i));//output  the list if less than 100 words
                                }
                            }
                            System.out.println("Total vocabulary words: "+ vocabList.size());
                            break;
                        case "index":
                            index = Indexer.buildIndex(corpusB,pathName);
                            break;
                        default:
                            Indexer.boolSearch(query,corpusB,dIndex);
                            System.out.println("Enter Document ID number to view contents or -1 to continue: ");
                            int docID = scan.nextInt();
                            scan.nextLine();
                            openDocument(docID,corpusB);
                            break;
                            }
                    //break;

                             }//end while
                             case 2:
						        System.out.println("Enter corpus path: ");
						        //scan.nextLine();
						        //String pathNameR = scan.nextLine();
                                String pathNameR = "/Users/berry/Desktop/moby";	
                                //String pathNameR = "/Users/berry/Desktop/CECS429/MobyDick10Chapters";//REMOVE 154
                                //String pathNameR = "/Users/berry/Desktop/CECS429/all-nps-sites-extracted";
						        System.out.println(Paths.get(pathNameR).toAbsolutePath());
						        DocumentCorpus corpusR = DirectoryCorpus.loadTextDirectory(Paths.get(pathNameR).toAbsolutePath());
						        corpusR.getDocuments();
						        DiskPositionalIndex d2 = new DiskPositionalIndex(pathNameR);
                                
						        while(true) {
							        System.out.println("Enter search query: ");
                                    //String query = "what similarity laws must be obeyed when constructing aeroelastic models of heated high speed aircraft";
							        String query = scan.nextLine();
                                    //String query = "whale";
                                    //String query = "whale";
                                    //String query = "camping in yosemite";
							        if(query.equals("q")) {
								    return;
							        }
                                    PriorityQueue<Accumulator> res = Indexer.rankedSearch(corpusR,d2,query);
                                    int resSize = res.size();
                                    System.out.println("Res Size: " + resSize);
                                    while(!res.isEmpty()){
                                        Accumulator currAcc = res.poll();
                                        String title = corpusR.getDocument(currAcc.getDocId()).getTitle();
                                        int docId = currAcc.getDocId() + 1;
                                        docId--;
                                        double value = currAcc.getA_d();
                                        System.out.println("Title: " + title.toString()+ " Doc ID: " + docId+ " Value: "+ value);
                                        return;
                                    }
								    
							}
                            case 3:
                            System.out.println("Enter corpus path: ");
                            //scan.nextLine();
                            String path = scan.nextLine();	
                            System.out.println(Paths.get(path).toAbsolutePath());
                            DocumentCorpus cor = DirectoryCorpus.loadTextDirectory(Paths.get(path).toAbsolutePath());
                            cor.getDocuments();
                            DiskPositionalIndex disk = new DiskPositionalIndex(path);

                            CalculatePrecision.meanAveragePrecision(path, cor, disk); 
                            return;   
						}
                     }
                

            }
        }
        public static void openDocument(int docID, DocumentCorpus corpus) throws IOException{
            //Get document contents the user wants
            if(docID>=0){
                //Get contents of Document user asked for
                BufferedReader bufferedReader = new BufferedReader(corpus.getDocument(docID).getContent());
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                //use bufferedReader to read each single character in line
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line); //building the string
                }
                String str = stringBuilder.toString(); //the string results
                System.out.println(str); //display
                bufferedReader.close(); //close reader
                //break;
            }
            else{
                System.out.println("Bad Input");
                return;
            }
        //return query;
    }
}
