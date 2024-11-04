package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.example.CustomAnalyzer;

public class DynamicAnalyzer {

    public static Analyzer getAnalyzer(String choice) {
        switch (choice.toLowerCase()) {
            case "simple":
                return new SimpleAnalyzer();
            case "whitespace":
                return new WhitespaceAnalyzer();
            case "custom":
                return new CustomAnalyzer();            
            case "english":
                return new EnglishAnalyzer();
            case "standard":
            default:
                return new StandardAnalyzer();
        }
    }
}
