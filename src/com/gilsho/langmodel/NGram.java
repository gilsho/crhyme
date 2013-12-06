package com.gilsho.langmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class NGram implements LanguageModel {
  
  public static final String START = "<S>";
  public static final String STOP = "</S>";
  public static final String UNKNOWN = "<UNK/>";
  
  protected int n;
  
  public NGram(int n) {
    this.n = n;
  }
  
  @Override
  public abstract void train(Collection<List<String>> trainingSentences);
  
  @Override
  public double getSentenceProbability(List<String> sentence) {
    List<String> stoppedSentence = new ArrayList<String>(sentence);
    stoppedSentence.add(STOP);
    for (int i = 0; i < n - 1; i++) {
      stoppedSentence.add(0, START);
    }
    double probability = 0.0;
    for (int index = n - 1; index < stoppedSentence.size(); index++) {
      probability += Math.log(getWordProbability(stoppedSentence, index)) / Math.log(2.0);
    }
    return probability;
  }

  public int getN() {
    return n;
  }

  @Override
  public double getWordProbability(List<String> sentence, int index) {
    return getWordProbability(getPrefix(sentence, index),
        sentence.get(index));
  }
  
  public abstract double getWordProbability(List<String> prefix, String word);

  protected List<String> getPrefix(List<String> sentence, int index) {
    List<String> prefix = new ArrayList<String>(sentence.subList(index - n + 1, index));
    assert prefix.size() == n - 1;
    return prefix;
  }
  
  public abstract Set<List<String>> knownPrefixes();
  
  public abstract Set<String> knownWords(List<String> prefix);
  
  @Override
  public double checkModel() {
    int checked = 0;
    double sum = 0.0;
    for (List<String> prefix : knownPrefixes()) {
      double sample = Math.random();
      // We expect to check ~10 distributions
      if (sample < 10.0 / knownPrefixes().size()) {
        checked++;
        sum += checkModelForPrefix(prefix);  
      }
    }
    // Check random prefixes;
    Object[] lexiconArray = lexicon().toArray();
    for (int iteration = 0; iteration < 10; iteration++) {
      List<String> prefix = new ArrayList<String>();
      for (int i = 0; i < n - 1; i++) {
        int wordIndex = (int) Math.random() * lexicon().size();
        assert 0 <= wordIndex && wordIndex < lexicon().size();
        prefix.add((String)lexiconArray[wordIndex]);
      }
      checked++;
      sum += checkModelForPrefix(prefix);
    }
    System.out.println("checked " + checked + " conditional probabilities");
    return sum / checked;
  }
  
  private double checkModelForPrefix(List<String> prefix) {
    assert prefix.size() == n - 1;
    double sum = 0.0;
    for (String word : lexicon()) {
      sum += getWordProbability(prefix, word);
    }
    sum += getWordProbability(prefix, UNKNOWN);
   
    if (Math.abs(1.0-sum) > 1e-6) {
      System.err.println("Model doesn't add to 1 for prefix " + prefix + ": " + sum);
    }
    return sum;
  }
  
  
  @Override
  public List<String> generateSentence() {
    List<String> sentence = new ArrayList<String>();
    List<String> prefix = new ArrayList<String>();
    for (int i = 0; i < n - 1; i++) {
      prefix.add(START);
    }
    String word;
    do {
      word = generateWord(prefix);
      sentence.add(word);
      prefix.add(word);
      prefix.remove(0);
    } while (!word.equals(STOP));
    return sentence;
  }
  
  protected abstract Set<String> lexicon();
  
  public String generateWord(List<String> prefix) {
    assert prefix.size() == n - 1;
    double sample = Math.random();
    double sum = 0.0;
    for (String word : lexicon()) {
      sum += getWordProbability(prefix, word);
      if (sum > sample) {
        return word;
      }
    }
    return UNKNOWN;
  }
  
  public List<String> chopPrefix(List<String> largePrefix) {
    assert largePrefix.size() >= n - 1;
    List<String> prefix = largePrefix.subList(largePrefix.size() + 1 - n, largePrefix.size());
    assert prefix.size() == n - 1;
    return prefix;
  }
  
}
