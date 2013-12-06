package com.gilsho.langmodel;

import com.gilsho.util.Counter;

import java.util.*;

public class EMInterpolatedNGramModel extends NGram implements TunableModel {

  private static final double MINIMUM_ENTROPY_STEP = 0.01;
  private static final int NUMBER_ITERATIONS = 10;
  private List<NGram> models;
  private NGram lastModel;
  private boolean modelTuned;
  private Map<List<String>, List<Double>> weights;
  private EmpiricalNGramModel ngram;
  private NGram backupModel;

  public EMInterpolatedNGramModel(List<NGram> models, NGram backupModel, int n) {
    super(n);
    this.models = models;
    for (int i = 0; i < models.size(); i++) {
      assert models.get(i).getN() <= n;
    }
    lastModel = models.get(models.size() - 1);
    // The last NGram should be of size exactly n.
    assert lastModel.getN() == n;
    modelTuned = false;
    weights = new HashMap<List<String>, List<Double>>();
    ngram = new EmpiricalNGramModel(n);
    this.backupModel = backupModel;
  }

  @Override
  public void tune(Collection<List<String>> trainingSentences) {
    modelTuned = true;
    ngram.train(trainingSentences);
    if (backupModel instanceof TunableModel) {
      ((TunableModel) backupModel).tune(trainingSentences);
    }

    for (List<String> prefix : ngram.knownPrefixes()) {
      Set<String> wordSet = ngram.knownWords(prefix);

      // Initialize weights.
      List<Double> weight = new ArrayList<Double>();
      weight.addAll(Collections.nCopies(models.size(), 1.0 / models.size()));
      weights.put(prefix, weight);

      // Initialize intermediate probabilities;
      // P(word, Y)
      Map<String, List<Double>> jointProbability = new HashMap<String, List<Double>>();
      // P(Y | word)
      Map<String, List<Double>> reverseConditional = new HashMap<String, List<Double>>();
      for (String word : wordSet) {
        jointProbability.put(word, new ArrayList<Double>());
        jointProbability.get(word).addAll(
            Collections.nCopies(models.size(), 0.0));
        reverseConditional.put(word, new ArrayList<Double>());
        reverseConditional.get(word).addAll(
            Collections.nCopies(models.size(), 0.0));
      }

      // Initialize the fractional counts;
      // E[N(word, Y)]
      List<Counter<String>> fractionalCount = new ArrayList<Counter<String>>();
      for (int i = 0; i < models.size(); i++) {
        fractionalCount.add(new Counter<String>());
      }
      
      double previousEntropy = Double.NEGATIVE_INFINITY;
      for (int iteration = 0; iteration < NUMBER_ITERATIONS; iteration++) {
        // E-Step 1- Compute P(word, Y) = P(word|Y) * P(Y)
        for (String word : wordSet) {
          for (int i = 0; i < models.size(); i++) {
            jointProbability.get(word).set(
                i,
                models.get(i).getWordProbability(
                    models.get(i).chopPrefix(prefix), word)
                    * weight.get(i));
          }
        }

        // E-step 2- Compute P(Y | word) by normalizing per word.
        // Compute entropy at the same time: H = SUM(Count(word) * P(word))
        double entropy = 0.0;
        for (String word : wordSet) {
          double total = 0.0;
          for (int i = 0; i < models.size(); i++) {
            total += jointProbability.get(word).get(i);
          }
          for (int i = 0; i < models.size(); i++) {
            reverseConditional.get(word).set(i,
                jointProbability.get(word).get(i) / total);
          }
          entropy += ngram.getCount(prefix, word) * Math.log(total);
        }

        // E-step 3- Distribute observed count according to P(Y| word)
        for (String word : wordSet) {
          for (int i = 0; i < models.size(); i++) {
            fractionalCount.get(i).setCount(
                word,
                ngram.getCount(prefix, word)
                    * reverseConditional.get(word).get(i));
          }
        }

        // M-step- Sum the above per model, divide by total word count.
        // that is the new P(Y).
        for (int i = 0; i < models.size(); i++) {
          weight.set(i, fractionalCount.get(i).totalCount()
              / ngram.getPrefixCounter(prefix).totalCount());
        }

        checkWeights(weight);
        
        if (entropy - previousEntropy < MINIMUM_ENTROPY_STEP) {
          break;
        }
        previousEntropy = entropy;
      }
    }
  }

  private void checkWeights(List<Double> weightList) {
    double sum = 0.0;
    for (Double d : weightList) {
      assert d > -1E-6;
      assert d < 1 + 1E-6;
      sum += d;
    }
    assert Math.abs(sum - 1.0) < 1E-6;
  }

  @Override
  public void train(Collection<List<String>> trainingSentences) {
    for (NGram model : models) {
      model.train(trainingSentences);
    }
    backupModel.train(trainingSentences);
  }

  @Override
  public double getWordProbability(List<String> prefix, String word) {
    assert modelTuned == true;
    if (!weights.containsKey(prefix)) {
      return backupModel.getWordProbability(prefix, word);
    }

    double probability = 0.0;
    for (int i = 0; i < models.size(); i++) {
      probability += models.get(i).getWordProbability(
          models.get(i).chopPrefix(prefix), word)
          * weights.get(prefix).get(i);
    }
    return probability;
  }

  @Override
  public double checkModel() {
    assert modelTuned == true;
    double sum = 0.0;
    for (NGram model : models) {
      sum += model.checkModel();
    }
    sum += super.checkModel();
    sum += backupModel.checkModel();
    return sum / (models.size() + 2);
  }

  @Override
  public List<Double> modelWeigths() {
    return new ArrayList<Double>();
  }

  @Override
  public Set<List<String>> knownPrefixes() {
    return lastModel.knownPrefixes();
  }

  @Override
  public Set<String> knownWords(List<String> prefix) {
    return lastModel.knownWords(prefix);
  }

  @Override
  protected Set<String> lexicon() {
    return lastModel.lexicon();
  }

}
