package io.github.akmal2409.utils;

public final class StringUtils {

  private StringUtils() {
    throw new IllegalStateException("Cannot instantiate a utility class");
  }

  public static boolean isEmpty(String text) {
    return text == null || text.isEmpty();
  }

  public static String toQuotedString(String text) {
    return String.format("\"%s\"", text);
  }
}
