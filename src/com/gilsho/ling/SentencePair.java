package com.gilsho.ling;

/**
 * Created with IntelliJ IDEA.
 * User: gilsho
 * Date: 11/28/13
 * Time: 3:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class SentencePair {

    public Sentence first;
    public Sentence second;

    public boolean firstReordered = false;
    public boolean secondReordered = false;
    public boolean firstSynonomyUsed = false;
    public boolean secondSynonmUsed = false;
    // public boolean firstParapharseUsed = false;
    // public boolean secondParaphraseUsed = false;

    public SentencePair(Sentence first, Sentence second) {

        this.first = first;
        this.second = second;
    }

    public SentencePair deepCopy() {
        SentencePair clone = new SentencePair(first.deepCopy(), second.deepCopy());
        return clone;
    }

    public String toString() {
        return first.toString() + "\n" + second.toString();
    }


}
