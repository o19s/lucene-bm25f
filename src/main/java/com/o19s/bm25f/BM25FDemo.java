package com.o19s.bm25f;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.BlendedTermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.PerFieldSimilarityWrapper;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

/**
 * Created by doug on 10/11/16.
 */
public class BM25FDemo {
    private static void addDoc(IndexWriter w, String title, String description) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("description", description, Field.Store.YES));
        w.addDocument(doc);
    }

    static Similarity perFieldSimilarities =  new PerFieldSimilarityWrapper() {
        @Override
        public Similarity get(String name) {
            if (name.equals("title")) {
                return new BM25FSimilarity(/*k1*/1.2f, /*b*/0.8f);
            } else if (name.equals("description")) {
                return new BM25FSimilarity(/*k1*/1.4f, /*b*/0.9f);
            }
            return new BM25FSimilarity();
        }
    };

    public static void main() throws IOException {



        // lots of boilerplate from http://www.lucenetutorial.com/lucene-in-5-minutes.html
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setSimilarity(perFieldSimilarities);

        IndexWriter w = new IndexWriter(index, config);

        addDoc(w, "Moby Dick", "Moby Dick was a pretty cool whale");
        addDoc(w, "The moby Letter", "I listen to moby!");

        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);


        BlendedTermQuery bm25fQuery = new BlendedTermQuery.Builder()
                                            .add(new Term("title", "moby"), 2.0f)
                                            .add(new Term("description", "moby"), 4.0f)
                                            .setRewriteMethod(BlendedTermQuery.BOOLEAN_REWRITE)
                                            .build();


        TopDocs docs = searcher.search(bm25fQuery, 10);
        ScoreDoc[] hits = docs.scoreDocs;

        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
        }
    }

}
