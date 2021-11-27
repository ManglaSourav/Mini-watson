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

public class LanguageModel {

    private static int numerOfHits = 10;
    IndexSearcher searcher;
    Analyzer analyzer;
    IndexReader indexReader;
    List<Queries> queries = new ArrayList<>();

    /**
     * Constructor: It loads the pre-built index and it also loads all 100 question in memory from the file
     */
    public LanguageModel() {
        try {
            String indexPath = Utils.getIndexPath();
            Directory indexDirectory = FSDirectory.open(Paths.get(indexPath));
            indexReader = DirectoryReader.open(indexDirectory);
            searcher = new IndexSearcher(indexReader);
            searcher.setSimilarity(new LMDirichletSimilarity());
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
                        query.setAnswer(Utils.clean(line));
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

    public static void main(String[] args) {
        try {
            LanguageModel lm = new LanguageModel();
            System.out.println("Language Model performance " + lm.applyLM());
            lm.indexReader.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method apply Language model on query one by one and measure performance using MMR and I’m using LMDirichletSimilarity  similarity as a scoring function which is giving me higher performance.
     *
     * @return Performance score of the language model
     * @throws IOException
     * @throws ParseException
     */
    public double applyLM() throws IOException, ParseException {

        double mmr_result = 0;

        for (Queries query : queries) {
            List<ResultClass> totalHits = LM_With_Dirichlet_smoothing(query.getClue() + " " + query.getQuestion(), numerOfHits);
//            System.out.println();
//            System.out.println(query.getAnswer());
//            System.out.println(totalHits.get(0).DocName.get("docid"));
            for (int rank = 0; rank < totalHits.size(); rank++) { // check if we have correct document in top 10 hits
                if (query.getAnswer().contains(totalHits.get(rank).DocName.get("title"))) {
                    mmr_result += (double) 1 / (rank + 1);
                    break; // break after we found first correct document on rank+1 position
                }

            }
        }

        mmr_result = mmr_result / (double) queries.size();
//        double pa1_result = (double) correct / queries.size();
//        Systexm.out.println("pa1_result@1 " + pa1_result);
        return mmr_result;
    }


    /**
     * This method has actual model and smoothing implementation for a given question.
     *
     * @param question :  asked question with clue
     * @param hits     : Number of docs to check
     * @return top score docs by language model
     * @throws ParseException
     * @throws IOException
     */
    public List<ResultClass> LM_With_Dirichlet_smoothing(String question, int hits) throws ParseException, IOException {
        Query parsedQuery = new QueryParser("content", analyzer).parse(QueryParser.escape(question));
        TopDocs topDocs = searcher.search(parsedQuery, hits);
        Map<Integer, List<String>> docs = new HashMap<>();
        List<ResultClass> finalResult = new ArrayList<>();
        String[] questionTokens = question.split(" ");

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            List<String> content = Arrays.asList(searcher.doc(scoreDoc.doc).get("content").split(" "));
            docs.put(scoreDoc.doc, content);
        }

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            int docID = scoreDoc.doc;
            Document doc = searcher.doc(docID);
            List<String> docTokens = docs.get(scoreDoc.doc);
            double totalProbabilityScoreOfDoc = 1;
            for (String qToken : questionTokens) {
                int tf = Collections.frequency(docTokens, qToken);
                double total = 0;
                double occurances = 0; //collection frequency
                for (int docid : docs.keySet()) {
//                    System.out.println(docMap.get(docid));
                    occurances += Collections.frequency(docs.get(docid), qToken);
                    total += docs.get(docid).size();
                }
                int Ld = docTokens.size();
                double PtMc = occurances / total;

                double score = (tf + 0.5 * PtMc) / (Ld + 0.5);
//                double PtMd = (double) tf / Ld;
//                double lambda = 0.5;
//                double score = lambda * PtMd + (1 - lambda) * PtMc;
                totalProbabilityScoreOfDoc *= score;
            }
            finalResult.add(new ResultClass(doc, totalProbabilityScoreOfDoc));
        }

        finalResult.sort(new Comparator<ResultClass>() {
            @Override
            public int compare(ResultClass r1, ResultClass r2) {
                return r2.docScore.compareTo(r1.docScore);
            }
        });
        return finalResult;
    }
}
