package org.example; 

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class CranfieldIndex {

    public static void main(String[] args) throws Exception {
        
        String pathIndex = "index"; 
        String pathDocument = "src/main/resources/cran.all.1400";
        
        String selectAnalyzer = args.length > 0 ? args[0] : "standard";

        Path dirDocument = Paths.get(pathDocument);
        if (!Files.isReadable(dirDocument)) {
            System.err.println("Unable to find '" + dirDocument.toAbsolutePath() + "' directory");
            System.exit(1);
        }

        Directory dir = FSDirectory.open(Paths.get(pathIndex));
        //use analyzer
        Analyzer analyzer = DynamicAnalyzer.getAnalyzer(selectAnalyzer);
        IndexWriterConfig inwicon = new IndexWriterConfig(analyzer);
        inwicon.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        //writes index to the directory
        IndexWriter writer = new IndexWriter(dir, inwicon);
        //start indexing the documents
        indexDocuments(writer, dirDocument);
        //close writer when finished
        writer.close();
        System.out.println("Finished indexing using " + selectAnalyzer + " analyzer.");
    }

    //indexed documents in cran.all.1400
    static void indexDocuments(IndexWriter writer, Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            BufferedReader buffeReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            String docuLine;
            String docuContent = null;
            StringBuilder storedText = new StringBuilder();
            //new Lucene document
            Document doc = new Document();
            
            //reads document line by line
            while ((docuLine = buffeReader.readLine()) != null) {
                docuLine = docuLine.trim();
                if (docuLine.startsWith(".I")) {
                    if (!storedText.toString().isEmpty()) {
                        doc.add(new TextField("content", storedText.toString(), Field.Store.YES));
                        //adds document to index
                        writer.addDocument(doc);
                        doc = new Document(); 
                        storedText.setLength(0); 
                    }
                    //adds document Id to new document
                    doc.add(new StringField("docID", docuLine.substring(3).trim(), Field.Store.YES));
                } else if (docuLine.startsWith(".T")) {
                    docuContent = "Title";
                } else if (docuLine.startsWith(".A")) {
                    docuContent = "Author";
                } else if (docuLine.startsWith(".B")) {
                    docuContent = "Bibliography";
                } else if (docuLine.startsWith(".W")) {
                    docuContent = "Abstract";
                } else {

                    if (docuContent != null) {
                        storedText.append(docuLine).append(" ");
                    }
                }
            }

            if (!storedText.toString().isEmpty()) {
                doc.add(new TextField("content", storedText.toString(), Field.Store.YES));
                writer.addDocument(doc);
            }
        }
    }
}