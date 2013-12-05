package com.gilsho;

import com.gilsho.rhymes.RhymeFetcher;
import com.gilsho.ling.SentencePair;
import com.gilsho.synonyms.SynonymFetcher;
import com.gilsho.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

public class Launcher {

    private static String readWord() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            return br.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    public static void printAllRhymesFor(String word) {
        for (String r : RhymeFetcher.getRhymes(word)) {
            System.out.println(r);
        }
    }

    private static void testParsing() {
        LexicalizedParser lp = LexicalizedParser.loadModel(
                "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
                "-maxLength", "80", "-retainTmpSubcategories");
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        String[] sent = { "This", "is", "an", "easy", "sentence", "."  };
        Tree parse = lp.apply(edu.stanford.nlp.ling.Sentence.toWordList(sent));
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();

        TreePrint tp = new TreePrint("penn");
        tp.printTree(parse);

    }

    private static void testSynonmys(String word) {
        for (String s : SynonymFetcher.getSynonyms(word)) {
            System.out.println(s.toString());
        }
    }

    private static void testSentenceMatching() {

        /*  I scrambled the following lyrics from U2 - One:

            "Did I ask too much, more than a lot
             You gave me nothing now it's all I got"
         */

        String[] s1 = {"Did", "I", "ask", "too", "much", ",", "more", "than", "a", "lot"};
        String[] s2 = {"Now", "all", "I", "got", "it's", "nothing", "you", "gave", "me"};

        SentencePair sentencePair = new SentencePair(new Sentence(s1), new com.gilsho.ling.Sentence(s2));

        CRhyme srm = new CRhyme();
        srm.match(sentencePair);

    }

    private static void testNaiveMatching() {
        //String[] s1 = {"Sleight", "of", "hand", "and", "twist", "of", "fate"};
        //String[] s2 = {"she", "tells", "me", "to", "hold", "on", "a", "bed", "of", "nails"};

//        String[] s1 = {"the", "way", "you", "show", "your", "care"};
//        String[] s2 = {"the", "way", "you're", "there", "always"};

          String[] s1 = {"yesterday", "I", "took", "my", "dog", "for", "a", "walk"};
          String[] s2 = {"I", "am", "doing", "do", "cook", "dinner", "now"};


        SentencePair sentencePair = new SentencePair(new Sentence(s1), new Sentence(s2));
        CRhyme srm = new CRhyme();
        for (SentencePair sp : srm.matchNaive(sentencePair)) {
            System.out.println("------> candidate");
            System.out.println(sp);
        }


    }

    private static void testSwitching() {
        LexicalizedParser lp = LexicalizedParser.loadModel(
                "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
                "-maxLength", "80", "-retainTmpSubcategories");
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        String[] sent = { "This", "is", "an", "easy", "sentence"};
        Tree parse = lp.apply(edu.stanford.nlp.ling.Sentence.toWordList(sent));
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();

        switchNodes(parse);

        TreePrint tp = new TreePrint("penn");
        tp.printTree(parse);

        System.out.println(flatten(parse));

    }

    private static String flatten(Tree t) {
        String str = "";
        for (Tree s : t.getLeaves()) {
            str += s.nodeString() + " ";
        }
        return str;
    }

    private static List<String> listify(String sent) {
        List<String> list = new ArrayList<String>();
        while (true) {
            int index = sent.indexOf(" ");
            if (index == -1) {
                list.add(sent);
                break;
            }
            list.add(sent.substring(0,index));
            sent = sent.substring(index+1);
        }
        return list;
    }

    private static void switchNodes(Tree t) {
        if (t.isEmpty())
            return;

        if (t.numChildren() == 2) {
            Tree tmp = t.children()[0];
            t.children()[0] = t.children()[1];
            t.children()[1] = tmp;
        }

        for (Tree s : t.getChildrenAsList()) {
            switchNodes(s);
        }
    }


    private static String lastToken(String phrase) {
        StringTokenizer strtok = new StringTokenizer(phrase);
        for (int i=0; i<strtok.countTokens()-1; i++) {
            strtok.nextElement();
        }
        return strtok.nextToken();
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("wordnet.database.dir", "/usr/local/WordNet-3.0/dict");
//        printAllRhymesFor("much");
//        testSentenceMatching();
//        testParsing();
//        testSynonmys("sunrise");
//        testPhrasal();
//        testWordNet("decision");
//        testSwitching();
        testNaiveMatching(); //latest
    }


}
