package com.gilsho;

import com.gilsho.ling.Sentence;
import com.gilsho.ling.SentencePair;
import com.gilsho.ling.SentencePairComparator;
import com.gilsho.rhymes.RhymeFetcher;
import com.gilsho.rhymes.RhymeList;
import com.gilsho.synonyms.SynonymFetcher;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.*;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: gilsho
 * Date: 11/28/13
 * Time: 2:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class CRhyme {


    private void populateSynMap(Sentence sent, Map<String, List<String>> synMap) {
        Tree root = sent.getParseTree();
        for (Tree t : root.getLeaves()) {
            boolean isNoun = true;
            String pos = t.parent(root).value();
            System.out.println(t.value() + ", " + pos);
            String word = t.nodeString();
            isNoun = (pos == "NN" || pos == "N" || pos == "NP");
            synMap.put(word, SynonymFetcher.getSynonyms(word));
            synMap.put(word, SynonymFetcher.getSynonyms(word));
        }
    }


    private void populateRhymeMap(Set <String> list, Map<String, RhymeList> rhymeMap) {

        System.out.println("rhymeMap has " + list.size() + " entries to populate");
        for (String originWord : list) {
            rhymeMap.put(originWord, RhymeFetcher.getRhymes(originWord));
        }

    }

    private void mergeStringLists(Map<String, List<String> > map, Set<String> accumulator) {
        for (String key : map.keySet()) {
            accumulator.add(key);
            accumulator.addAll(map.get(key));
        }
    }

    public PriorityQueue<SentencePair> generateCandidateMatches(SentencePair sentencePair) {

        Map<String, RhymeList> rhymeMap = new HashMap<String, RhymeList>();
        Map<String, List<String>> synMapFirst = new HashMap<String, List<String>>();
        Map<String, List<String>> synMapSecond = new HashMap<String, List<String>>();

        System.out.println("populating first syn map...");
        populateSynMap(sentencePair.first, synMapFirst);
        System.out.println("populating second syn map...");
        populateSynMap(sentencePair.second, synMapSecond);

        System.out.println("merging syn maps...");
        Set<String> allRhymes = new HashSet<String>();
        mergeStringLists(synMapFirst, allRhymes);
        mergeStringLists(synMapSecond, allRhymes);

        System.out.println("populating rhyme maps...");
        populateRhymeMap(allRhymes, rhymeMap);

        Comparator<SentencePair> comparator = new SentencePairComparator();
        int capacity =  100;
        PriorityQueue<SentencePair> results = new PriorityQueue<SentencePair>(capacity, comparator);

        for (int i=sentencePair.first.getWordList().size()-1; i>=0; i--) {
            String examinedFirst = sentencePair.first.getWordList().get(i);
            List<String> matchAgainst = synMapFirst.get(examinedFirst);
            if (matchAgainst == null) continue;
            for (String s : matchAgainst) {
                RhymeList rlist = rhymeMap.get(s);
                if (rlist == null) continue;
                for (int j=sentencePair.second.getWordList().size()-1; j>=0; j--) {
                    String examinedSecond = sentencePair.second.getWordList().get(j);
                    List<String> candidates = synMapSecond.get(examinedSecond);
                    if (candidates == null) continue;
                    for (String t : candidates) {

                        if (s.equals(t)) continue;

                        String r = rlist.findRhyme(t);
                        if (r == null) continue;

                        System.out.println("FOUND MATCH: [" + examinedFirst + " =>" + s + "] " +
                           "rhymes with: [" + examinedSecond + " => " + t + "]");

                        SentencePair pair = sentencePair.deepCopy();

                        //check if synonym used in first sentence
                        pair.first.getWordList().set(i, s);

                        //check if first sentence needs re-ordering
                        if (i != sentencePair.first.getWordList().size()-1) {
                            Tree root = pair.first.getParseTree();
                            pair.first = new Sentence(flatten(pushToEnd(root.getLeaves().get(i), root)));
                        }

                        //check if synonym used in second sentence
                        pair.second.getWordList().set(j, t);

                        //check if second sentence needs re-ordering
                        if (j != sentencePair.second.getWordList().size()-1) {
                            Tree root = pair.second.getParseTree();
                            pair.second = new Sentence(flatten(pushToEnd(root.getLeaves().get(j), root)));
                        }

                        results.add(pair);

                    }
                }
            }
        }

        return results;
    }

    private static List<String> flatten(Tree t) {
        List<String> list = new ArrayList<String>();
        for (Tree s : t.getLeaves()) {
            list.add(s.nodeString());
        }
        return list;
    }


    private Tree pushToEnd(Tree t, Tree root) {

        if (t == null)
            return null;

        Tree parent = t.parent(root);

        if (parent == null)
            return t;

        int last = parent.numChildren()-1;
        if (parent.getChild(last) != t) {
            //make t the last child
            for (int i=0; i<parent.numChildren(); i++) {
                if (parent.getChild(i) == t) {
                    parent.children()[i] = parent.getChild(last);
                    parent.children()[last] = t;
                }
            }

        }

        return pushToEnd(parent, root);
    }



    private void printMap(Map<String, List<String> > map) {
        for (String key : map.keySet()) {
            System.out.print(key + "=> [");
            for (String val : map.get(key)) {
                System.out.print(val + ", ");
            }
            System.out.println("]");
        }
    }

    private void printTree(Sentence s) {
        LexicalizedParser lp = LexicalizedParser.loadModel(
                "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
                "-maxLength", "80", "-retainTmpSubcategories");
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        String[] sarr = (String[]) s.toStringArray(s.getWordList());
        Tree parse = lp.apply(edu.stanford.nlp.ling.Sentence.toWordList(sarr));
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
        TreePrint tp = new TreePrint("penn");
        tp.printTree(parse);
    }


}