package org.example; 

import java.util.Arrays; 

public class MainApp {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.exit(1);
        }

        //select between indexing, searching ot both
        String mode = args[0];
        //select analyzer
        String selectAnalyzer = args[1];
        //select scoring approach
        String selectScoring = args[2];

        //excecute if index or both
        if (mode.equals("index") || mode.equals("both")) {
            System.out.println("Executing CranfieldIndex");
            CranfieldIndex.main(new String[]{selectAnalyzer});
        }
        //excecute if search or both
        if (mode.equals("search") || mode.equals("both")) {
            System.out.println("Executing CranfieldSearch");
            CranfieldSearch.main(new String[]{selectAnalyzer, selectScoring});
        }
        System.out.println("Finished");
    }
}
