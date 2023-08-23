package io.github.akmal2409.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringUtils {

  public static boolean isEmpty(@Nullable String string) {
    return string == null || string.isBlank();
  }

  public static String padLeft(@NotNull String s, int targetLength, char paddingChar) {
    if (s == null) throw new NullPointerException("Cannot pad string that is null");
    else if (s.length() >= targetLength) return s;

    final StringBuilder sb = new StringBuilder();

    for (int padCount = targetLength - s.length(); padCount > 0; padCount--) {
      sb.append(paddingChar);
    }

    for (int i = 0; i < s.length(); i++) sb.append(s.charAt(i));

    return sb.toString();
  }
}
