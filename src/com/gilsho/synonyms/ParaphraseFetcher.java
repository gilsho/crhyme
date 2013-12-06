package com.gilsho.synonyms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: gilsho
 * Date: 12/5/13
 * Time: 12:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class ParaphraseFetcher {

    private static final String filepath = "/Users/gilsho/Desktop/NLP/ppdb-1.0-s-all";

    private static Map<String,List<String>> paraphraseTable = null;

    private static void populatePhraseTable() {
        paraphraseTable = new HashMap<String, List<String>>();
        try {
            BufferedReader bf = new BufferedReader(new FileReader(filepath));
            while (true) {
                String line = bf.readLine();
                if (line == null) {
                    break;
                }
                String delimiter = "||| ";
                int startIndex = line.indexOf(delimiter);
                int middleIndex = line.indexOf(delimiter, startIndex+delimiter.length());
                int lastIndex = line.indexOf(delimiter, middleIndex+delimiter.length());
                String firstPhrase = line.substring(startIndex + delimiter.length(), middleIndex-1);
                String secondPhrase = line.substring(middleIndex + delimiter.length(), lastIndex -1);
                System.out.println(firstPhrase + " " + secondPhrase);
            }

        } catch(IOException e) {

        }
    }

    public static List<String> getPhrases(String word) {

        if (paraphraseTable == null) {
            populatePhraseTable();
        }


        List<String> list = new ArrayList<String>();
        return list;

    }

}
