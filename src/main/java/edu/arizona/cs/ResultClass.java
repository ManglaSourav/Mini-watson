/*
Titla - Jeopardy Project
Name- Sourav Mangla
Subject - CSC 483/583: Text Retrieval and Web Search
Instructor - Mihai Surdeanu
TA - Shahriar Golchin
 */
package edu.arizona.cs;

import org.apache.lucene.document.Document;

public class ResultClass {
    Document DocName;
    Double docScore = 0.0;

    public ResultClass() {

    }

    public ResultClass(Document doc, double docScore) {
        this.DocName = doc;
        this.docScore = docScore;
    }

    @Override
    public String toString() {
        return "ResultClass{" +
                "DocName=" + DocName +
                ", docScore=" + docScore +
                '}';
    }
}
