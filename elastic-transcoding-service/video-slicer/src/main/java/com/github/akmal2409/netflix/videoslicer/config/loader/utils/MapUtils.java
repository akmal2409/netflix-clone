package com.github.akmal2409.netflix.videoslicer.config.loader.utils;

import java.util.Map;
import java.util.stream.Collectors;

public final class MapUtils {

  private MapUtils() {
    throw new IllegalStateException("Cannot instantiate a utility class");
  }

  /**
   * Returns a string of the following format:
   * key=value separated by the separator
   */
  public static String toKVString(Map<?, ?> map, String separator) {
    return map.entrySet()
               .stream()
               .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
               .collect(Collectors.joining(separator));
  }
}
