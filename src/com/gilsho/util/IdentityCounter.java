package com.gilsho.util;

/**
 * Convenience Extension of Counter to use an IdentityHashMap.
 *
 * @author Dan Klein
 */
public class IdentityCounter<E> extends Counter<E> {
  static final long serialVersionUID = 1L;

  public IdentityCounter() {
    super(new com.gilsho.util.MapFactory.IdentityHashMapFactory<E,Double>());
  }
}
