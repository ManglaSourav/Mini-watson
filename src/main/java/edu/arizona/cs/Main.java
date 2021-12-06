/*
Titla - Jeopardy Project
Name- Sourav Mangla
Subject - CSC 483/583: Text Retrieval and Web Search
Instructor - Mihai Surdeanu
TA - Shahriar Golchin
 */
package edu.arizona.cs;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.*;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {

        Engine engine = new Engine();
        LanguageModel lm = new LanguageModel();

        System.out.println("It won't take more than 1 minute to run all the models");
        System.out.println("Applying BM25Similarity");
        System.out.println("P@1: " + engine.Pa1());
        System.out.println("MRR: " + engine.MRR());
//        System.out.println("MAP: " + engine.MAP());

        System.out.println("\nApplying BooleanSimilarity");
        engine.setSimilarity(new BooleanSimilarity());
        System.out.println("P@1: " + engine.Pa1());
        System.out.println("MRR: " + engine.MRR());

        System.out.println("\nApplying MultiSimilarity : BooleanSimilarity & BM25Similarity & TFIDF_Similarity");
        engine.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(), new BooleanSimilarity(), new TFIDF_Similarity()}));
        System.out.println("P@1: " + engine.Pa1());
        System.out.println("MRR: " + engine.MRR());

        System.out.println("\nPart 5 soln");
        System.out.println("Language Model performance " + lm.applyLM());


        System.out.println("\nBonus part soln");
        System.out.println("Word2vec is processing....");
        Word2Vec word2Vec = new Word2Vec();
        System.out.println("MRR for word2vec : " + word2Vec.MRR());

        word2Vec.indexReader.close();
        lm.indexReader.close();
        engine.indexReader.close();
    }
}
