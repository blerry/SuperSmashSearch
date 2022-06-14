package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.indexes.DiskIndexWriter;
import cecs429.indexes.DiskPositionalIndex;
import cecs429.indexes.Index;
import cecs429.indexes.PositionalInvertedIndex;
import cecs429.indexes.Posting;
import cecs429.queries.Accumulator;
import cecs429.queries.BooleanQueryParser;
import cecs429.queries.TermLiteral;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
    
public class Indexer {
    private static final int RANKED_RETURN = 50;
    public static void main(String[] args) throws IOException {
        //"/Users/berry/Desktop/CECS429/all-nps-sites-extracted"
        // /Users/berry/Desktop/CECS429/testCorpus
        // /Users/berry/Desktop/cor
        // Create a DocumentCorpus to load .txt documents from the project directory.
        Index index;
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
                index = indexDiskCorpus(corpus, s);

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
                    System.out.println("Enter corpus path: ");
                    scan.nextLine();
                    String pathName = scan.nextLine();	
                    System.out.println(Paths.get(pathName).toAbsolutePath());
                    DocumentCorpus corpusB = DirectoryCorpus.loadTextDirectory(Paths.get(pathName).toAbsolutePath());
                    corpusB.getDocuments();
                    DiskPositionalIndex d = new DiskPositionalIndex(pathName);
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
                            List<String> vocabList = index.getVocabulary(); //make a temp vocab list from vocab
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
                            index = buildIndex(corpus,s);
                            break;
                        default:
                            search(query,corpus,index);
                            System.out.println("Enter Document ID number to view contents or -1 to continue: ");
                            int docID = scan.nextInt();
                            scan.nextLine();
                            openDocument(docID,corpus);
                            break;
                            }
                    break;

            }
            }//end while
        }
        //Move this to another class later on
        public String webSearch(String query,DocumentCorpus corpus, Index index){
            List<Posting> postings = search(query, corpus, index);//Run Boolean Search
            StringBuilder postingsRows = new StringBuilder();
            String result = "";
            for (Posting post : postings) {//include document titles for each returned posting

            String title = corpus.getDocument(post.getDocumentId()).getTitle();
            String row = "    <tr>\n" +
                        "        <td>"+post.getDocumentId()+"</td>\n" +
                        "        <td><button id=\"" + post.getDocumentId() + "\" onClick=\"docClicked(this.id)\" >"+title+"</button></td>\n" +
                        "        <td>"+post.getPostions()+"</td>\n" +
                        "    </tr>\n";
                postingsRows.append(row);

            }

            result = "<div><b>Query: </b>" + query +
                    "<div>Total Documents: " + postings.size() + "</div></div></br>" +
                    "<table style=\"width:100%\">\n" +
                    "    <tr>\n" +
                    "        <th>Document ID</th>\n" +
                    "        <th>Document Title</th>\n" +
                    "        <th>Positions</th>\n" +
                    "    </tr>\n" +
                    postingsRows.toString() +
                    "</table>";
            return result;
        }
        //Boolean search
        public static List<Posting> search(String query,DocumentCorpus corpus, Index index){
            BooleanQueryParser parser = new BooleanQueryParser(); //boolean for terms
            List<Posting> postings = parser.parseQuery(query).getPostings(index);
                int docCount = 0; //doc counter
                //get the postings of the query after parsing  using index
                for(Posting p: postings){
                    System.out.println(p.getDocumentId() + ". " + corpus.getDocument(p.getDocumentId()).getTitle());
                    docCount++;
                    System.out.println(p.getPostions());
                }
                System.out.println("Number of Documents: " + docCount);
                return postings;
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
            //return query;
        }
        public static Index buildIndex(DocumentCorpus corpus, String path){
            DiskIndexWriter diskIndexWriter = new DiskIndexWriter();

            long startTime = System.nanoTime();
            Index index = indexCorpus(corpus);
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            // Create a DocumentCorpus to load .txt documents from the project directory.
            System.out.println("Corpus indexed in: " + totalTime / 1000000000 + " seconds");
            try {
                diskIndexWriter.writeIndex(index, path);
            } catch (IOException e) {
                // Auto-generated catch block
                System.out.println("Something went wrong.");
                e.printStackTrace();
            }
            return index;
        }
        public String stemWord(String word){
            ArrayList<String> stemmedWord = new AdvancedTokenProcessor().processToken(word);
            return stemmedWord.get(0);
        }
        //ranked query
    public static PriorityQueue<Accumulator> userRankedQueryInput(DocumentCorpus corpus, Index index, String queryInput) {
        double n = corpus.getCorpusSize();
        List<TermLiteral> termLiterals = new ArrayList<TermLiteral>();
        int counter = 0;
        List<Posting> postings = new ArrayList<Posting>();
        HashMap<Posting, Double> hm = new HashMap<>();
        PriorityQueue<Accumulator> pq = new PriorityQueue<>(RANKED_RETURN);

        String[] terms = queryInput.split(" ");
        for (String term : terms) { // for each term in query
            term = term.toLowerCase();
            String stemmedTerm = AdvancedTokenProcessor.stemToken(term);
            termLiterals.add(new TermLiteral(stemmedTerm));

            int df_t = index.getDocumentFrequencyOfTerm(stemmedTerm);
            double w_qt = Math.log(1 + n/df_t);  // calculate wqt = ln(1 + N/dft)
            //not as accurate, but saves us from thousands of disk reads
                postings = termLiterals.get(counter).getPostings(index);
                counter++;
                double tf_td = (double) index.getTermFrequency(stemmedTerm) / (double) postings.size();
                for(Posting p : postings){ // for each document in postings list
                    //Document d = corpus.getDocument(p.getDocumentId());//very slow
                    //double tf_td = index.getTermDocumentFrequency(stemmedTerm, d.getId());//Horribly slow
                    double w_dt = 1 + Math.log(tf_td);
                    double a_d = (w_dt * w_qt);
                    if (hm.get(p) != null) {
                        hm.put(p, hm.get(p) + a_d);
                    } else {
                        hm.put(p, a_d);
                    }
                }
            }

        List<Accumulator> accumulators = new ArrayList<Accumulator>();
        hm.forEach((key,value) -> accumulators.add(new Accumulator(key.getDocumentId(),value)));
        for (Accumulator acc : accumulators){
            // only retain the a certain amount of the top results
            double value = acc.getA_d() / index.getDocumentWeight(acc.getDocId());
            acc.setA_d(value);
            if(pq.size() < RANKED_RETURN || pq.peek().getA_d() < acc.getA_d()){
                if(pq.size() == RANKED_RETURN){
                    pq.remove();
                }
                pq.add(acc);
            }
        }

        return pq;
    }
        public static Index indexDiskCorpus(DocumentCorpus corpus,String indexLocation) {
            PositionalInvertedIndex index = new PositionalInvertedIndex();//create positional index
            AdvancedTokenProcessor processor = new AdvancedTokenProcessor();//create token processor
            DiskIndexWriter diskIndexWriter = new DiskIndexWriter();
            ArrayList<Double> documentWeight = new ArrayList<>();
            // Get all the documents in the corpus by calling GetDocuments().
            Iterable<Document> documents = corpus.getDocuments();
            HashMap<String, Integer> mostPopularTerms = new HashMap<>();
            int currentDoc = 0;
            String[] vectorTerms = {"flow", "on", "at", "by", "that", "pressur", "an", "be", "number", "boundari", "layer", "from", "as", "result", "this", "it", "effect", "which", "method", "theori", "bodi", "solut", "heat", "wing", "mach", "equat", "shock", "use", "present", "was", "surfac", "distribut", "obtain", "two", "temperatur", "ratio", "been", "problem", "were", "veloc", "approxim", "calcul", "case", "have", "test", "plate", "investig", "given", "condit", "speed", "these", "valu", "transfer", "wave", "or", "has", "angl", "experiment", "superson", "jet", "made", "cylind", "edg", "rang", "measur", "laminar", "found", "load", "can", "stream", "lift", "determin", "coeffici", "analysi", "over", "increas", "general", "reynold", "wall", "free", "base", "high", "point", "turbul", "dimension", "also", "between", "some", "hyperson", "stress", "shown", "than", "buckl", "separ"};
            double[][] termVectorSpace = new double[corpus.getCorpusSize()][vectorTerms.length];

            for (Document docs : documents) {//iterate through every valid document found in the corpus
                currentDoc = docs.getId();
                int totalTerms = 0;
                double[] docVector = new double[vectorTerms.length];
                HashMap<String, Integer> termFrequency = new HashMap<>();//term frequency of every term in a document
                // Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
                EnglishTokenStream stream = new EnglishTokenStream(docs.getContent());
                Iterable<String> tokens = stream.getTokens();//convert read data into tokens
                int wordPosition = 1;//maintain the position of the word throughout the document
                // Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
                for (String token : tokens) {

                    List<String> words = processor.processToken(token);//convert a token to indexable terms
                    for (int i = 0; i < words.size(); i++) {//iterate through all unstemmed tokens
                        words.set(i, AdvancedTokenProcessor.stemToken(words.get(i)));
                        if (termFrequency.containsKey(words.get(i))) {//if term is duplicate
                            int prevFrequency = termFrequency.get(words.get(i));
                            termFrequency.put(words.get(i), prevFrequency + 1);//increment term frequency counter
                        } else {
                            termFrequency.put(words.get(i), 1);//add new term to frequency counter
                    }
                }
                index.addTerm(words, docs.getId(), wordPosition);//add word data to index
                wordPosition++;//increment word position
                totalTerms = words.size();
            }

            /* Determine popular terms */
            int finalTotalTerms = totalTerms;
            termFrequency.forEach((key, value) -> {

                for (int j = 0; j < vectorTerms.length; j++) {
                    if (key.equals(vectorTerms[j])) {
                        docVector[j] = (double) value / finalTotalTerms;
                    }
                }

                if (mostPopularTerms.containsKey(key)) {
                    int prevFrequency = mostPopularTerms.get(key);
                    mostPopularTerms.put(key, prevFrequency + value);
                } else {
                    mostPopularTerms.put(key, 1);
                }

            });

            for (int j = 0; j < docVector.length; j++) {
                termVectorSpace[currentDoc][j] = docVector[j];
            }

            /* */

            double sumTermWeights = 0;//sum of term weights
            ArrayList<Integer> tf_d = new ArrayList<>(termFrequency.values());//every term frequency in the document

            for (int i = 0; i < tf_d.size(); i++) {//iterate through all term frequencies
                double w_dt = 1 + Math.log(tf_d.get(i));//weight of specific term in a document
                w_dt = Math.pow(w_dt, 2);
                sumTermWeights += w_dt;//summation of w_dt^2
            }
            //do math to get L_d
            double l_d = Math.sqrt(sumTermWeights);//square root normalized w_dt's
            documentWeight.add(l_d);

        }
        //write document weights to disk
        diskIndexWriter.writeDocumentWeights(documentWeight, indexLocation);
        return index;
        }

        public static Index indexCorpus(DocumentCorpus corpus) {
            //HashSet<String> vocabulary = new HashSet<>();
            AdvancedTokenProcessor processor = new AdvancedTokenProcessor();	
            PositionalInvertedIndex  index = new PositionalInvertedIndex();

            // Get all the documents in the corpus by calling GetDocuments().
            Iterable<Document> documents = corpus.getDocuments();
            List<String> wordList = new ArrayList<String>();
            int position = 0;
            int docCount = 0;
            for (Document d : documents) {
                docCount++;
                EnglishTokenStream stream = new EnglishTokenStream(d.getContent());
                Iterable<String> tokens = stream.getTokens();//convert read data into tokens
                for(String token : tokens){
                    //String term = processor.processToken(token); //get 1 token at a time
                    wordList = processor.processToken(token);
                    //for (int i = 0; i < words.size(); i++) {
                    index.addTerm(wordList,d.getId(),position); //required because must know 
                    position++;
                }
                try{
                    stream.close(); //close stream
                }catch(IOException exe){
                    exe.printStackTrace(); //in case of backlash
                    }
            position = 0; //reset position
            }
            System.out.println("Number of Documents: " + docCount);
            return index;
        }
    }
    
