package io.github.akmal2409.job;

import io.github.akmal2409.utils.StringUtils;
import java.util.regex.Pattern;

public final class SegmentConstants {

  private SegmentConstants() {
    throw new IllegalStateException("Cannot instantiate a utility class");
  }

  public static final String SEGMENT_FILE_NAME_PATTERN = "segment-%08d.mp4";
  public static final Pattern SEGMENT_FILE_NAME_REGEX_PATTERN = Pattern.compile("^segment-(?<index>\\d{8})\\.mp4$");
  public static final String SEGMENT_FILE_NAME_INDEX_CAPTURING_GROUP_NAME = "index";

  /**
   * Converts segment index to file name with padding
   * @param index
   * @return
   */
  public static String segmentIndexToFileName(int index) {
    return String.format("segment-%s.mp4", StringUtils.padLeft(String.valueOf(index),
        8, '0'));
  }
}
