package com.gilsho.langmodel;


public class BackoffBiGramModel extends BackoffModel {

  public BackoffBiGramModel() {
    super(new SmoothNGramModel(2), new SmoothNGramModel(1));
  }

}
