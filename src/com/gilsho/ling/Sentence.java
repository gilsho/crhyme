package com.gilsho.ling;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: gilsho
 * Date: 12/4/13
 * Time: 1:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class Sentence {

    static LexicalizedParser lp = LexicalizedParser.loadModel(
            "/Users/gilsho/Desktop/NLP/final/src/edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
            "-maxLength", "80", "-retainTmpSubcategories");

    private List<String> words;
    private Tree tree;


    public Sentence() {
        words = new ArrayList<String>();
    }

    public Sentence(String sentenceString) {
        words = new ArrayList<String>();
        StringTokenizer strtok = new StringTokenizer(sentenceString);
        while (strtok.hasMoreTokens()) {
            String nextToken = strtok.nextToken();
            if (nextToken.equals("."))
                continue;
            words.add(stripTrailingCommaOrPeriod(nextToken.toLowerCase()));
        }
    }

    public Sentence(List<String> list) {
        words = new ArrayList<String>();
        for (String s : list) {
            if (s.equals("."))
                continue;
            words.add(stripTrailingCommaOrPeriod(new String(s).toLowerCase()));
        }
    }

    public Sentence(String[] array) {
        words = new ArrayList<String>();
        for (String s : array) {
            if (s.equals("."))
                continue;
            words.add(stripTrailingCommaOrPeriod(s.toLowerCase()));
        }
    }


    public List<String> getWordList() {
        return words;
    }

    public Sentence deepCopy() {
        Sentence s = new Sentence();
        for (String w : words) {
            s.getWordList().add(w);
        }
        return s;
    }

    public Tree getParseTree() {
        if (tree == null) {
            tree = lp.apply(edu.stanford.nlp.ling.Sentence.toWordList(toStringArray(words)));
        }
         return tree;
    }

    public String[] toStringArray(List<String> list) {
        String[] arr = new String[list.size()];
        for (int i=0; i<list.size();i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    public String toString() {
        String str = "";
        for (String s : words) {
            str += s + " ";
        }
        return str;
    }


    public static String stripTrailingCommaOrPeriod(String str) {
        int lastPosition = str.length()-1;
        if (str.charAt(lastPosition) == ',' || str.charAt(lastPosition) == '.') {
            return str.substring(0,str.length()-1);
        }
        return str;
    }

    public static boolean containsInvalidChars(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isLetter(c) || c == '-' || c == '\'' || c == '\"' ) {
                continue;
            }
            return true;
        }
        return false;
    }

}