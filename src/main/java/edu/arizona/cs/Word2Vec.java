/*
Titla - Jeopardy Project
Name- Sourav Mangla
Subject - CSC 483/583: Text Retrieval and Web Search
Instructor - Mihai Surdeanu
TA - Shahriar Golchin
 */
package edu.arizona.cs;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class Word2Vec {
    private static int numerOfHits = 10;
    IndexSearcher searcher;
    Analyzer analyzer;
    IndexReader indexReader;
    List<Queries> queries;
    HashMap<String, ArrayList<Double>> trainedEmbeddings;

    /**
     * Constructor: It loads the pre-built index and pre-trained embeddings from "glove.6B.100d.txt" and it also loads all 100 question in memory and calculate questions' word2vec embeddings
     */
    public Word2Vec() {
        try {
            queries = new ArrayList<>();
            trainedEmbeddings = new HashMap<>();
            loadGloveEmbeddings();
            String indexPath = Utils.getIndexPath();
            Directory indexDirectory = FSDirectory.open(Paths.get(indexPath));
            indexReader = DirectoryReader.open(indexDirectory);
            searcher = new IndexSearcher(indexReader);
            searcher.setSimilarity(new LMDirichletSimilarity());
            analyzer = new StandardAnalyzer();
            File queryFile = new File(Word2Vec.class.getClassLoader().getResource(Utils.getQueriesFilePath()).toURI());
            try (Scanner inputScanner = new Scanner(queryFile)) {
                int nextQuestion = 2;
                Queries query = new Queries();
                while (inputScanner.hasNextLine()) {
                    String line = inputScanner.nextLine();

                    if (nextQuestion == -1) {
                        nextQuestion = 2;
                        continue;
                    } else if (nextQuestion == 0) {
                        query.setAnswer(Utils.cleanAndTrim(line));
                        queries.add(query);
                        query = new Queries();
                    } else if (nextQuestion == 1) {
                        // get the embedding for the query and average the embeddings for each word
                        String askedQuery = Utils.cleanAndTrim(line);
                        query.setEmbedding(avgEmbeddingForSentence(askedQuery));
                        query.setQuery(askedQuery);
                    } else if (nextQuestion == 2) {
                        query.setClue(Utils.cleanAndTrim(line));
                    }
                    nextQuestion--;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Word2Vec word2Vec = new Word2Vec();
        System.out.println("MRR in word2vec : " + word2Vec.MRR());
    }

    /**
     * This is a helper function to calculate average embedding for a sentence.
     *
     * @param sentence : calculate word2vec embedding for the sentence
     * @return embedding vector
     */
    private ArrayList<Double> avgEmbeddingForSentence(String sentence) {
        String keyWord = "";
        ArrayList<Double> sentenceEmbedding = new ArrayList<>();
        String[] words = sentence.split("\\s+");
        int i = 0; // flag to extract word from embedding
        int numberOfWordsUsedInEmbeddings = 0;

        for (int position = 0; position < words.length; position++) {
            if (!Utils.getStopWordsList().contains(words[position])) { // restrict stop words
                numberOfWordsUsedInEmbeddings++;
                ArrayList<Double> wordEmbedding = trainedEmbeddings.get(words[position]);
                if (wordEmbedding != null) {
                    if (!wordEmbedding.isEmpty()) {
                        for (int valPosition = 0; valPosition < wordEmbedding.size(); valPosition++) {
                            Double value = wordEmbedding.get(valPosition);
                            if (sentenceEmbedding.size() < wordEmbedding.size()) {
                                sentenceEmbedding.add(value);
                            } else {
                                sentenceEmbedding.set(valPosition, sentenceEmbedding.get(valPosition) + value);
                            }
                        }
                    }
                }
            }
        }
        for (int valPosition = 0; valPosition < sentenceEmbedding.size(); valPosition++) {
            sentenceEmbedding.set(valPosition, sentenceEmbedding.get(valPosition) / numberOfWordsUsedInEmbeddings);
        }
//        System.out.println(sentenceEmbedding.size());
        return sentenceEmbedding;
    }

    /**
     * This method hold load pre-tained glove embedding into the memory
     */
    private void loadGloveEmbeddings() {
        try {
            File queryFile = new File(Word2Vec.class.getClassLoader().getResource(Utils.getEmbeddingPath()).toURI());
            try (Scanner inputScanner = new Scanner(queryFile)) {
                while (inputScanner.hasNextLine()) {
                    String keyWord = "";
                    ArrayList<Double> keyWordEmbeddingVector = new ArrayList<>();
                    String line = inputScanner.nextLine();
                    String[] splited = line.split("\\s+");
                    int i = 0; // flag to extract word from embedding
                    for (String val : splited) {
                        if (i == 0) {
                            keyWord = val;
                            i = 1;
                        } else {
                            keyWordEmbeddingVector.add(Double.valueOf(val));
                        }
                    }
                    trainedEmbeddings.put(keyWord, keyWordEmbeddingVector);
                }
//                System.out.println("loaded keyword embeddings " + trainedEmbeddings.keySet().size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method implement the MRR performance measure
     */
    public double MRR() {
        try {
            double mrr = 0;
            for (Queries question : queries) {
                List<ResultClass> totalHits = searchInLucene(question);
//                System.out.println("Actual answer " + question.getAnswer());
//                System.out.println("resulted Answer " + totalHits.get(0).DocName.get("title"));

                if (question.getAnswer().contains(totalHits.get(0).DocName.get("title"))) {
                    mrr += 1.0; // if document found on first position;
                } else {
                    for (int rank = 0; rank < totalHits.size(); rank++) { // check if we have correct document in top 10 hits
//                        System.out.println(totalHits.get(rank).docScore);
                        if (totalHits.get(rank).DocName.get("title").contains(question.getAnswer())) {
                            mrr += (double) 1 / (rank + 1);
//                            System.out.println("at position " + rank + "  " + totalHits.get(rank).DocName.get("title"));
                            break; // break after we found first correct document on rank+1 position
                        }
                    }
                }
            }
            return (double) mrr / queries.size();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    /**
     * This method search our question's answer in the lucene and build embedding vector for the docs and
     * perform cosine similarity between doc vs query embedding vector, then return the result
     *
     * @param query : object containing question, clue, answer, and question embedding vector
     * @return list of docs which are reranked using cosine scores
     */
    public List<ResultClass> searchInLucene(Queries query) throws ParseException, IOException {

        String question = query.getClue() + " " + query.getQuestion();
        Query parsedQuery = new QueryParser("content", analyzer).parse(QueryParser.escape(question));
        TopDocs topDocs = searcher.search(parsedQuery, numerOfHits);
        Map<Integer, List<String>> docs = new HashMap<>();
        List<ResultClass> finalResult = new ArrayList<>();
        ArrayList<Double> questionEmbedding = query.getEmbedding();

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            String docContent = searcher.doc(scoreDoc.doc).get("content");
            // I have removed all the special character so instead of selecting first sentence, I'm selecting sentence of length 150
            ArrayList<Double> docEmbedding = avgEmbeddingForSentence(docContent.substring(0, Math.min(docContent.length(), 150)));
            Document doc = searcher.doc(scoreDoc.doc);
            Double cosineScore = calculateCosineScore(questionEmbedding, docEmbedding);
            finalResult.add(new ResultClass(doc, cosineScore));
        }
        //sorting documents on the basis of new cosine score
        finalResult.sort(new Comparator<ResultClass>() {
            @Override
            public int compare(ResultClass r1, ResultClass r2) {
                return r2.docScore.compareTo(r1.docScore);
            }
        });
        return finalResult;
    }

    /**
     * This method calculate cosine similarity between question and document embedding vectors
     *
     * @param questionEmbedding : question embedding vector
     * @param docEmbedding      : document embedding vector
     * @return cosine score of two embedding vector
     */
    private Double calculateCosineScore(ArrayList<Double> questionEmbedding, ArrayList<Double> docEmbedding) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < questionEmbedding.size(); i++) {
            dotProduct += questionEmbedding.get(i) * docEmbedding.get(i);
            normA += Math.pow(questionEmbedding.get(i), 2);
            normB += Math.pow(docEmbedding.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}



