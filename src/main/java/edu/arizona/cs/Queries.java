/*
Titla - Jeopardy Project
Name- Sourav Mangla
Subject - CSC 483/583: Text Retrieval and Web Search
Instructor - Mihai Surdeanu
TA - Shahriar Golchin
 */
package edu.arizona.cs;


import java.util.ArrayList;

class Queries {
    private String clue;
    private String question;
    private String answer;
    private ArrayList<Double> embedding;

    public Queries() {
        String clue = "";
        String question = "";
        String answer = "";
        embedding = new ArrayList<>();

    }

    public ArrayList<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(ArrayList<Double> embedding) {
        this.embedding = embedding;
    }

    public String getClue() {
        return clue;
    }

    public void setClue(String clue) {
        this.clue = clue;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuery(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "Query{" +
                "category='" + clue + '\'' +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                '}';
    }
}

