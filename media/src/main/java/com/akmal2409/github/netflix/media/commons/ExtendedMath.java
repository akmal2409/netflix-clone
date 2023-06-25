package com.akmal2409.github.netflix.media.commons;

/**
 * Utility class providing extended mathematical computations.
 */
public final class ExtendedMath {

  private ExtendedMath() { throw new IllegalStateException("Cannot construct an instance of the utility class"); }

  /**
   * Computes Greatest Common Divisor using Euclidean algorithm.
   * The formula is following gcd(a, b) = gcd(a, a mod b) where a >= b and mod is modulo (remainder).
   * We can do it iteratively till we get a remainder of 0, then the previous remainder is our GCD.
   *
   * @param a first number
   * @param b second number
   * @return gcd - greatest common divisor.
   */
  public static int gcd(int a, int b) {
    if (a < 0) a = -a;
    if (b < 0) b = -b;

    if (b > a) return gcd(b, a);

    int lastRemainder = b;
    int currentRemainder = a % b;

    while (currentRemainder > 0) {
      lastRemainder = currentRemainder;

      currentRemainder = a % b;
      b = currentRemainder;
    }

    return lastRemainder;
  }
}
