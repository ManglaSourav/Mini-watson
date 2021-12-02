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
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class Engine {
    IndexSearcher searcher;
    Analyzer analyzer;
    IndexReader indexReader;
    List<Queries> queries = new ArrayList<>();
    Similarity similarity = new BM25Similarity();

    /**
     * Constructor: It loads the pre built index and it also loads all 100 question in memory from the file
     */
    public Engine() {
        try {
            String indexPath = Utils.getIndexPath();
            Directory indexDirectory = FSDirectory.open(Paths.get(indexPath));
            indexReader = DirectoryReader.open(indexDirectory);
            searcher = new IndexSearcher(indexReader);
            analyzer = new StandardAnalyzer();

            File queryFile = new File(Engine.class.getClassLoader().getResource(Utils.getQueriesFilePath()).toURI());

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
                        query.setQuery(line.trim().toLowerCase());

                    } else if (nextQuestion == 2) {
                        query.setClue(line.trim().toLowerCase());
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

    public static void main(String[] args) throws IOException, ParseException {
        Engine engine = new Engine();
        System.out.println("MMR: " + engine.MMR());
        engine.indexReader.close();
    }

    /**
     * @param similarity setter for the similarity
     */
    public void setSimilarity(Similarity similarity) {
        this.similarity = similarity;
    }

    /**
     * This method implement the P@1 performance measure
     *
     * @return the performance score.
     */
    public double Pa1() {
        int correct = 0;
        int hits = 1;
        for (Queries query : queries) {
            List<ResultClass> ans = searchInLucene(query.getClue() + " " + query.getQuestion(), hits);
//            System.out.println(query.getAnswer());
//            System.out.println(ans.get(0).DocName.get("title"));
            if (query.getAnswer().contains(ans.get(0).DocName.get("title"))) {
                correct++;
            }
        }
        return (double) correct / queries.size();
    }

    /**
     * This method implement the P@1 performance measure
     *
     * @return the performance score.
     */
    public double MMR() {
        try {
            double mmr_result = 0;
            int hits = 10;

            double mmr = 0;
            for (Queries question : queries) {
                List<ResultClass> totalHits = searchInLucene(question.getClue() + " " + question.getQuestion(), hits);
//                System.out.println();
//                System.out.println("Actual answer " + question.getAnswer());
//                System.out.println("resulted Answer " + totalHits.get(0).DocName.get("title"));

                if (question.getAnswer().contains(totalHits.get(0).DocName.get("title"))) {
                    mmr += 1.0; // if document found on first position;
                } else {
                    for (int rank = 0; rank < totalHits.size(); rank++) { // check if we have correct document in top 10 hits
                        if (totalHits.get(rank).DocName.get("title").contains(question.getAnswer())) {
                            mmr += (double) 1 / (rank + 1);
//                            System.out.println("at position " + rank + "  " + totalHits.get(rank).DocName.get("title"));
                            break; // break after we found first correct document on rank+1 position
                        }
                    }
                }
            }
            mmr_result = (double) mmr / queries.size();
            return mmr_result;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    /**
     * This method implement the MAP performance measure But it perform similar to MMR because we have only one relevant document
     *
     * @return the performance score.
     */
    public double MAP() {
        try {
            //m = number of relevant document is one in this problem, so MAP become similar to MMR

            double map_result = 0;
            int hits = 50;
            double map = 0;

            for (Queries question : queries) {
                List<ResultClass> totalHits = searchInLucene(question.getClue() + " " + question.getQuestion(), hits);
                for (int rank = 0; rank < totalHits.size(); rank++) { // check the cut point in all document
                    if (totalHits.get(rank).DocName.get("title").contains(question.getAnswer())) {
                        map += (double) 1 / (rank + 1);
                        break; // cut after we found first relevant document at rank+1 position
                    }
                }
            }
            map_result = (double) map / queries.size();

            return map_result;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    /**
     * This method search our question's answer in the lucene and return the result
     *
     * @param query : It is a question we want to get an answer for
     * @param hits  : Number of hit docs we want to consider
     * @return list of docs which are returned by the lucene
     */
    public List<ResultClass> searchInLucene(String query, int hits) {
        List<ResultClass> resultDocs = new ArrayList<>();
        Utils.setDoLemma(true);
//        Utils.setDoStemming(true); //lowering the performance
        query = Utils.lemmas_and_stem(Utils.cleanAndTrim(query));
        try {
            Query q = new QueryParser("content", analyzer).parse(query);
            searcher.setSimilarity(similarity);
            TopDocs docs = searcher.search(q, hits);
            ScoreDoc[] hitsDocs = docs.scoreDocs;

            for (int i = 0; i < hitsDocs.length; i++) {
                ResultClass resultDoc = new ResultClass();
                int docId = hitsDocs[i].doc;
                resultDoc.DocName = searcher.doc(docId);
                resultDoc.docScore = (double) hitsDocs[i].score;
                resultDocs.add(resultDoc);
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return resultDocs;
    }

}