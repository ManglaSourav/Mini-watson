package edu.arizona.cs;

import org.apache.lucene.search.similarities.TFIDFSimilarity;

public class TFIDF_Similarity extends TFIDFSimilarity {

    @Override
    public float tf(float freq) {
        return (float) Math.sqrt(freq);
    }

    @Override
    public float idf(long docFreq, long numDocs) {
        return (float) (Math.sqrt(numDocs / (docFreq + 1)) + 1);
    }

    @Override
    public float lengthNorm(int numTerms) {
        return (float) (1 / Math.sqrt(numTerms));
    }
}
