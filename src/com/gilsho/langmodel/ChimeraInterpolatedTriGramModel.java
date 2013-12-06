package com.gilsho.langmodel;

public class ChimeraInterpolatedTriGramModel extends InterpolatedTriGramModel {

  public ChimeraInterpolatedTriGramModel() {
    super(new SmoothNGramModel(1), new BackoffBiGramModel(),
        new BackoffTriGramModel());
  }

}
