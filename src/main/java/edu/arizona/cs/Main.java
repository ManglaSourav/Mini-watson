package edu.arizona.cs;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.*;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {

        Engine engine = new Engine();
        LanguageModel lm = new LanguageModel();
//        System.out.println("P@1: " + engine.Pa1());
//        System.out.println("MMR: " + engine.MMR());
//        System.out.println("MAP: " + engine.MAP());
//        System.out.println("Language Model performance " + lm.applyLM());

//        engine.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(), new BooleanSimilarity(), new LMDirichletSimilarity()}));
//        System.out.println("P@1: " + engine.Pa1());
//        System.out.println("MMR: " + engine.MMR());

//      Applying language model
//        engine.setSimilarity(new BM25Similarity());
//        System.out.println("P@1: " + engine.Pa1());
//        System.out.println("MMR: " + engine.MMR());
//        we.MRRBenchMark(100, false);

        lm.indexReader.close();
        engine.indexReader.close();
    }
}
