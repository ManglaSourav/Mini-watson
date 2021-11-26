package edu.arizona.cs;


import edu.stanford.nlp.simple.Sentence;
import org.tartarus.snowball.ext.PorterStemmer;

import java.net.URI;
import java.net.URISyntaxException;

public class Utils {

    private final static String queriesFilePath = "questions.txt";
    private final static String wikiPagesDataPath = "wiki-data";
    private final static String indexPath = System.getProperty("user.dir") + "/src/main/resources/wiki-index";
    private final static int Number_Of_Docs_Rerank = 10;
    private static boolean doStemming = false;
    private static boolean doLemma = false;

    public static int getNumber_Of_Docs_Rerank() {
        return Number_Of_Docs_Rerank;
    }

    public static void setDoStemming(boolean doStemming) {
        Utils.doStemming = doStemming;
    }

    public static void setDoLemma(boolean doLemma) {
        Utils.doLemma = doLemma;
    }

    public static URI getTrainingDataPath() throws URISyntaxException {
        return Utils.class.getClassLoader().getResource(wikiPagesDataPath).toURI();
    }

    public static String getIndexPath() {
        return indexPath;
    }

    public static String getQueriesFilePath() {
        return queriesFilePath;
    }

    public static String clean(String str) {
        return str.replaceAll("\n", " ").replaceAll("\\[tpl\\].*\\[\\/tpl\\]", " ").replaceAll("[^ a-zA-Z\\d]", " ")
                .toLowerCase()
                .trim();
    }

    public static String preprocessText(String str) {
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

    public static String getStemWord(String word) {
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent();
    }

}
