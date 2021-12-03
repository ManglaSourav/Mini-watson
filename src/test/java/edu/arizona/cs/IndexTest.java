package edu.arizona.cs;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;

public class IndexTest {
    @Test
    public void testTotalDocInIndex() {

        try {
            String indexPath = Utils.getIndexPath();
            Directory indexDirectory = FSDirectory.open(Paths.get(indexPath));
            IndexReader indexReader = DirectoryReader.open(indexDirectory);
            assertThat("Number for indexed docs ", indexReader.numDocs() > 279000);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
