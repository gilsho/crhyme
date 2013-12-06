package com.gilsho.ling;

import com.gilsho.langmodel.BackoffZipfTriGramModel;
import com.gilsho.langmodel.LanguageModel;
import com.gilsho.util.Sentences;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gilsho
 * Date: 12/5/13
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class SentencePairComparator implements Comparator<SentencePair> {

    private static final String DATAFILE = "/Users/gilsho/Desktop/NLP/final/data/europarl-train.sent.txt";

    private static LanguageModel model;

    private static void trainModel() {
        Collection<List<String>> trainSentences;
        try {
            model = BackoffZipfTriGramModel.class.newInstance();
            trainSentences =
                    Sentences.Reader.readSentences(DATAFILE);
        }   catch(Exception e) {
            System.err.println(e.toString());
            return;
        }

        model.train(trainSentences);
    }

    private boolean unModified(SentencePair sp) {
        if (sp.firstReordered == false &&
            sp.firstSynonomyUsed == false &&
            sp.secondReordered == false &&
            sp.secondSynonmUsed == false) {
            return true;
        }
        return false;
    }

    private boolean noSynonmsUsed(SentencePair sp) {
        if (sp.firstSynonomyUsed == false &&
            sp.secondSynonmUsed == false) {
            return true;
        }
        return false;
    }

    private boolean noReordering(SentencePair sp) {
        if (sp.firstReordered== false &&
            sp.secondReordered == false) {
            return true;
        }
        return false;
    }

    private double scoreSentence(SentencePair sp) {
        if (model == null) {
            trainModel();
        }

        double score1 = model.getSentenceProbability(sp.first.getWordList());
        double score2 = model.getSentenceProbability(sp.second.getWordList());

        return score1 * score2;
    }

    @Override
    public int compare(SentencePair sp1, SentencePair sp2) {


        if (unModified(sp1)) {
            return 1;
        }

        if (unModified(sp2)) {
            return -1;
        }

        if (noReordering(sp1)) {
            return 1;
        }

        if (noReordering(sp2)) {
            return -1;
        }

        double score1 = scoreSentence(sp1);
        double score2 = scoreSentence(sp2);
        if (score1 <= score2) {
            return 1;
        } else {
            return -1;
        }

    }
}
