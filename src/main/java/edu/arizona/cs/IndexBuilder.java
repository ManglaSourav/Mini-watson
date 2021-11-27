/*
Titla - Jeopardy Project
Name- Sourav Mangla
Subject - CSC 483/583: Text Retrieval and Web Search
Instructor - Mihai Surdeanu
TA - Shahriar Golchin
 */
package edu.arizona.cs;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class IndexBuilder {

    public static void main(String[] args) throws URISyntaxException {
        IndexBuilder builder = new IndexBuilder();
        builder.buildIndex();
    }

    /*
      This method build out index.
      Read all files from wiki data directory and process each file.
    */
    public void buildIndex() {
        System.out.println("Index Builder is running and your index is building...");
        System.out.println("Please wait for some time -:) ");
        try {
            File indexFile = new File(Utils.getIndexPath());
            StandardAnalyzer analyzer = new StandardAnalyzer();
            Directory index = FSDirectory.open(indexFile.toPath());
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(index, config);

            File[] allFiles = new File(Utils.getTrainingDataPath()).listFiles();
            if (allFiles != null) {
                int processedFileCount = 0;
                for (File file : allFiles) {
                    processedFileCount++;
                    System.out.println("processing file number... " + processedFileCount);
                    putFileToIndex(file, writer);
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method build out index.
     * Read all files from wiki data directory and process each file.
     *
     * @param file   : preprocess the given file and add content to the index
     * @param writer : index writer object
     */
    public void putFileToIndex(File file, IndexWriter writer) {
        System.out.println("Processing file " + file);
        try (Scanner inputScanner = new Scanner(file)) {
            String title = "";
            String categories = "";
            StringBuilder headings = new StringBuilder();
            StringBuilder result = new StringBuilder();

            Matcher PageStarterMatcher;
            Matcher headingMatcher;
            while (inputScanner.hasNextLine()) {
                String line = inputScanner.nextLine();
                if (line.equals('\n')) {
                    continue;
                } else if ((headingMatcher = Pattern.compile("\\=\\=.*\\=\\=\\n?").matcher(line)).matches()) {
                    String temp = Utils.clean(headingMatcher.toMatchResult().group().replace('=', ' ').trim() + " ");
                    if (temp.isEmpty() || temp == null) {
                        continue;
                    }
                    headings.append(temp).append(" ");
                } else if (line.indexOf("CATEGORIES:") == 0) {
                    categories = Utils.clean(line.substring(12));
                } else if ((PageStarterMatcher = Pattern.compile("\\[\\[.*\\]\\]\\n?").matcher(line)).matches()) {
                    // Whenever i got new wiki page in a file I dumped the last page and create new page for current wiki page;
                    if (!title.equals("")) {
                        addDoc(writer, title, categories, headings.toString(), result.toString());
                    }
                    title = Utils.clean(PageStarterMatcher.toMatchResult().group().replace('[', ' ').replace(']', ' ').trim());
                    result = new StringBuilder();
                    categories = "";
                    headings = new StringBuilder();

                } else {
                    result.append(Utils.clean(line));
                    result.append(" ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * add wiki page to index
     *
     * @param writer     : index writer
     * @param title      : title of the wiki page
     * @param categories : only categories inside pages
     * @param headings   : only heading inside a wiki page
     * @param content    : all content of a wiki page
     * @throws IOException
     */
    private void addDoc(IndexWriter writer, String title, String categories, String headings, String content) throws IOException {
        Document newDoc = new Document();
        content = categories + " " + headings + " " + content;
//        Utils.setDoLemma(true);
//        Utils.setDoStemming(true);
        newDoc.add(new StringField("title", title, Field.Store.YES));
        newDoc.add(new TextField("categories", Utils.lemmas_and_stem(categories), Field.Store.YES));
        newDoc.add(new TextField("content", Utils.lemmas_and_stem(content), Field.Store.YES));
        writer.addDocument(newDoc);
    }

}
