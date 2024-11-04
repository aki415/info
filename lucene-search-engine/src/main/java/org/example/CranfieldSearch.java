package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CranfieldSearch {

    public static void main(String[] args) throws Exception {
        String pathIndex = "index";  
        //path to search queries
        String pathQuery = "src/main/resources/cran.qry"; 
        
        //select analyzer
        String selectAnalyzer = args.length > 0 ? args[0] : "english";
        //select scoring approach
        String selectScoring = args.length > 1 ? args[1] : "bm25"; 
        //save results as a text file
        String resultsPath = "result" + "_" + selectAnalyzer + "_" + selectScoring + ".txt";
        //reads indexed documents
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(pathIndex)));
        //searches documents
        IndexSearcher searcher = new IndexSearcher(reader);
        
        switch (selectScoring.toLowerCase()) {
            case "vsm":
                searcher.setSimilarity(new ClassicSimilarity());
                break;
            case "lm-dirichlet":
                searcher.setSimilarity(new LMDirichletSimilarity());
                break;
            case "boolean":
                searcher.setSimilarity(new BooleanSimilarity());
                break;
            case "bm25":
            default:
                searcher.setSimilarity(new BM25Similarity());
                break;
        }

        Analyzer analyzer = DynamicAnalyzer.getAnalyzer(selectAnalyzer);

        BufferedReader qryReader = Files.newBufferedReader(Paths.get(pathQuery), StandardCharsets.UTF_8);
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(resultsPath), StandardCharsets.UTF_8);

        String docuLine;
        StringBuilder qryText = new StringBuilder();
        int queryId = 1;
        //reads cranfield query file cran.qry
        while ((docuLine = qryReader.readLine()) != null) {
            if (docuLine.startsWith(".I")) {
                if (qryText.length() > 0) {

                    performSearch(searcher, analyzer, qryText.toString().trim(), Integer.toString(queryId), writer);
                    //clear for next query
                    qryText.setLength(0);
                    queryId++; 
                }
            } else if (docuLine.startsWith(".W")) {
                qryText.setLength(0);
            } else {
                qryText.append(docuLine).append(" ");
            }
        }

        if (qryText.length() > 0) {
            performSearch(searcher, analyzer, qryText.toString().trim(), Integer.toString(queryId), writer);
        }

        writer.close();
        reader.close();
        System.out.println("Finished search, results saved to " + resultsPath);
    }

    private static void performSearch(IndexSearcher searcher, Analyzer analyzer, String qryStr, String queryID, BufferedWriter writer) throws Exception {

        qryStr = QueryParser.escape(qryStr);
        
        QueryParser qryparser = new QueryParser("content", analyzer);
        Query query = qryparser.parse(qryStr);
        //performs search on index and retrieve top 1000 results
        TopDocs results = searcher.search(query, 1000);
        ScoreDoc[] searchResults = results.scoreDocs;
        //rank search results
        int rank = 0;
        for (ScoreDoc resultDoc : searchResults) {
            Document document = searcher.doc(resultDoc.doc);
            String documentId = document.get("docID");
            rank++;
            //write result
            writer.write(String.format("%s 0 %s %d %.6f STANDARD%n", queryID, documentId, rank, resultDoc.score));
        }
    }
}