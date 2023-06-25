package com.akmal2409.github.netflix.media.commons;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExtendedMathTest {

  @Test
  @DisplayName("gcd function computes correct result when second number is greater than the first")
  void gcdCorrectResultForSecondNumberLarger() {
    assertGCD(12, 16, 4);
  }
  @Test
  @DisplayName("gcd function computes correct results for two positive integers")
  void gcdCorrectResultForTwoPositiveIntegers() {
    assertGCD(16, 12, 4);
  }

  @Test
  @DisplayName("gcd function computes correct results for two negative integers")
  void gcdCorrectResultForTwoNegativeIntegers() {
    assertGCD(-16, -12, 4);
  }

  @Test
  @DisplayName("gcd function computes correct results for negative and positive integers")
  void gcdCorrectResultForNegativeAndPositiveInteger() {
    assertGCD(-16, 12, 4);
  }

  private static void assertGCD(int firstNumber, int secondNumber, int expectedGcd) {
    assertThat(ExtendedMath.gcd(firstNumber, secondNumber))
        .isEqualTo(expectedGcd);
  }
}
