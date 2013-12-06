package com.gilsho;

import com.gilsho.langmodel.BackoffZipfTriGramModel;
import com.gilsho.langmodel.LanguageModel;
import com.gilsho.ling.Sentence;
import com.gilsho.ling.SentencePair;
import com.gilsho.rhymes.RhymeFetcher;
import com.gilsho.synonyms.ParaphraseFetcher;
import com.gilsho.synonyms.SynonymFetcher;
import com.gilsho.util.Sentences;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.*;

import java.io.*;
import java.util.*;

//import com.gilsho.langmodel.BackoffZipfTriGramModel;
//import com.gilsho.langmodel.LanguageModel;

public class Launcher {

    private static Sentence readSentence() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            String str = br.readLine();
            return new Sentence(str);
        } catch (IOException e) {
            return null;
        }
    }

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

    private static void testLanguageModel() {
        LanguageModel model;
        Collection<List<String>> trainSentences;
        try {
            model = (LanguageModel) BackoffZipfTriGramModel.class.newInstance();
            trainSentences =
                Sentences.Reader.readSentences("/Users/gilsho/Desktop/NLP/final/data/europarl-train.sent.txt");
        }   catch(Exception e) {
            System.err.println(e.toString());
            return;
        }

        model.train(trainSentences);
        while (true) {
            System.out.println("Enter a sentence:");
            Sentence sent = readSentence();
            double score = model.getSentenceProbability(sent.getWordList());
            System.out.println("Score: " + score);
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


    private static void testNaiveMatching() {

//        String[] s1 = {"yesterday", "I", "took", "my", "dog", "for", "a", "walk"};
//        String[] s2 = {"I", "am", "going", "to", "cook", "dinner", "now"};

        while (true) {

            System.out.println("Enter first sentence:");
            Sentence s1 = readSentence();
            System.out.println("Enter second sentence:");
            Sentence s2 = readSentence();

            SentencePair sentencePair = new SentencePair(s1, s2);

            System.out.println("Generating candidates...");
            CRhyme srm = new CRhyme();
            for (SentencePair sp : srm.generateCandidateMatches(sentencePair)) {
                System.out.println("------> candidate");
                System.out.println(sp);
            }

        }


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

    private static void testParaphraseTable() {
        List<String> list = ParaphraseFetcher.getPhrases("hit the jackpot");
        for (String s : list) {
            System.out.println(s);
        }
    }


    private static String lastToken(String phrase) {
        StringTokenizer strtok = new StringTokenizer(phrase);
        for (int i=0; i<strtok.countTokens()-1; i++) {
            strtok.nextElement();
        }
        return strtok.nextToken();
    }

    private static void processFile(String infilepath, String outfilepath) {
        CRhyme crhyme = new CRhyme();

        final String horizontalLine = "--------------------------\n";

        try {
            BufferedReader br = new BufferedReader(new FileReader(infilepath));
            BufferedWriter bw = new BufferedWriter(new FileWriter(outfilepath));
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                StringTokenizer strtok = new StringTokenizer(line,"|");
                String first = strtok.nextToken();
                String second = strtok.nextToken();
                String gold = strtok.nextToken();

                if (first == null || second == null || gold == null) {
                    System.err.println("Error. found null string");
                }

                Sentence s1 = new Sentence(first);
                Sentence s2 = new Sentence(second);
                Sentence sg = new Sentence(gold);

                SentencePair sp = new SentencePair(s1, s2);
                SentencePair spg = new SentencePair(s1, sg);

                PriorityQueue<SentencePair> results = crhyme.generateCandidateMatches(sp);
                bw.write("Original Sentence:\n" + horizontalLine);
                bw.write(sp.toString() + "\n\n");
                bw.write("Gold Standard:\n" + horizontalLine);
                bw.write(spg.toString() + "\n\n");
                bw.write("Candidates:\n" + horizontalLine);
                for (SentencePair pair : results) {
                    bw.write(pair.toString() + "\n\n");
                }
                bw.write("=========================\n\n");

            }

            bw.close();
            br.close();

        } catch(IOException e) {
            System.err.println(e);
        }

    }

    public static void main(String[] args) throws Exception {
        System.setProperty("wordnet.database.dir", "/usr/local/WordNet-3.0/dict");
//        printAllRhymesFor("earphone");
//        testParsing();
//        testSynonmys("sunrise");
//        testSwitching();
//        testParaphraseTable();
//        testLanguageModel();
//        testNaiveMatching(); //latest

        processFile("/Users/gilsho/Desktop/NLP/final/data/test-short.txt",
                "/Users/gilsho/Desktop/NLP/final/data/out-short.txt");
    }

}
