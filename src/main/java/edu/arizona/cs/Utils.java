/*
Titla - Jeopardy Project
Name- Sourav Mangla
Subject - CSC 483/583: Text Retrieval and Web Search
Instructor - Mihai Surdeanu
TA - Shahriar Golchin
 */
package edu.arizona.cs;

import edu.stanford.nlp.simple.Sentence;
import org.tartarus.snowball.ext.PorterStemmer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class Utils {

    private final static String queriesFilePath = "questions.txt";
    private final static String wikiPagesDataPath = "wiki-data";
    private final static String indexPath = System.getProperty("user.dir") + "/src/main/resources/wiki-index";
    private final static String embeddingPath = "glove.6B/glove.6B.100d.txt";
    private static final String[] stopWordsList = new String[]{
            "a", "an", "about", "actually", "and", "are", "as", "at", "be", "but", "by",
            "for", "if", "in", "into", "is", "it", "s",
            "no", "not", "of", "on", "or", "such",
            "that", "the", "their", "then", "there", "these",
            "they", "this", "to", "was", "will", "with"};
    private static boolean doStemming = false;
    private static boolean doLemma = false;

    public static List getStopWordsList() {
        return Arrays.asList(stopWordsList);
    }

    public static void main(String[] args) {

    }

    public static String getEmbeddingPath() {
        return embeddingPath;
    }

    /**
     * @param doStemming : setter for stemming operation, true means in future we want to stemming everywhere
     */
    public static void setDoStemming(boolean doStemming) {
        Utils.doStemming = doStemming;
    }

    /**
     * @param doLemma : setter for lemmatization operation, true means in future we want to lemmatization everywhere
     */
    public static void setDoLemma(boolean doLemma) {
        Utils.doLemma = doLemma;
    }

    /**
     * @return URI of the wiki pages
     * @throws URISyntaxException
     */
    public static URI getTrainingDataPath() throws URISyntaxException {
        return Utils.class.getClassLoader().getResource(wikiPagesDataPath).toURI();
    }

    /**
     * @return Path of the index
     */
    public static String getIndexPath() {
        return indexPath;
    }

    /**
     * @return Path of the question file
     */
    public static String getQueriesFilePath() {
        return queriesFilePath;
    }


    /**
     * @param str : Clean tpl tags from this string
     * @return cleaned string
     */
    public static String cleanTPLTags(String str) {
        return str.replaceAll("\\[\\s*tpl\\s*\\]", " ")
                .replaceAll("\\[\\s*/\\s*tpl\\s*\\]", " ");
    }

    /**
     * @param str : Clean links from this string
     * @return cleaned string
     */
    public static String cleanLinks(String str) {
        return str.replaceAll("https ?://\\\\S+\\\\s?", "");
    }


    /**
     * This method remove tpl tags and special character present in the text
     *
     * @param str : Apply clean operation on the string
     * @return cleaned string
     */
    public static String cleanAndTrim(String str) {
        return str
                .replaceAll("[^ a-zA-Z\\d]", " ").toLowerCase()
                .replaceAll("\n", " ").trim();
    }

    /**
     * This method do stemming and lemmatization conditionally
     *
     * @param str : Perform Lemmatization and stemming on the string str
     * @return conditional lemmatized and stemmed str
     */
    public static String lemmas_and_stem(String str) {
        if (doLemma) {
//            System.out.println("Before lemma and stemming " + str);
            StringBuilder lemmaContainer = new StringBuilder();
            if (str == null || str.isEmpty()) {
                return str;
            }
            for (String lemma : new Sentence(str.toLowerCase()).lemmas()) {
                lemmaContainer.append(lemma).append(" ");
            }
            str = lemmaContainer.toString();

        }

        if (doStemming) {
            StringBuilder stemContainer = new StringBuilder();
            if (str == null || str.isEmpty()) {
                return str;
            }
            for (String word : new Sentence(str.toLowerCase()).words()) {
                stemContainer.append(getStemWord(word)).append(" ");
            }
            str = stemContainer.toString();
        }
//        System.out.println("After lemmas and stemming  " + str);

        return str;
    }

    /**
     * This method return a stem work of a given word
     *
     * @param word : Token to stem
     * @return a stem work of a given word
     */
    public static String getStemWord(String word) {
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent();
    }

}
