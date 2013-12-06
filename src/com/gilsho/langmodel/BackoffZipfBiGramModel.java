package com.gilsho.langmodel;


public class BackoffZipfBiGramModel extends BackoffModel {

  public BackoffZipfBiGramModel() {
    super(new ZipfSmoothNGramModel(2), new ZipfSmoothNGramModel(1));
  }

}
