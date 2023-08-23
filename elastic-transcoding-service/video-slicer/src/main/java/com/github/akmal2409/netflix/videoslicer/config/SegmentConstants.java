package com.github.akmal2409.netflix.videoslicer.config;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SegmentConstants {

  private SegmentConstants() {
    throw new IllegalStateException("Cannot instantiate class with constants");
  }

  public static final String SEGMENT_FILE_NAME_PATTERN = "segment-%08d.mp4";
  public static final Pattern SEGMENT_FILE_NAME_REGEX_PATTERN = Pattern.compile("^segment-(?<index>\\d{8})\\.mp4$");
  public static final String SEGMENT_FILE_NAME_INDEX_CAPTURING_GROUP_NAME = "index";

  /**
   * The comparator assumes that the input file names are already valid and contain named capturing group.
   */
  public static final Comparator<String> SEGMENT_FILE_NAME_COMPARATOR = (firstName, secondName) -> {

    final Matcher firstMatcher = SegmentConstants.SEGMENT_FILE_NAME_REGEX_PATTERN.matcher(firstName);
    final Matcher secondMatcher = SegmentConstants.SEGMENT_FILE_NAME_REGEX_PATTERN.matcher(secondName);

    firstMatcher.find();
    secondMatcher.find();

    final int firstIndex = Integer.parseInt(firstMatcher.group(SEGMENT_FILE_NAME_INDEX_CAPTURING_GROUP_NAME));
    final int secondIndex = Integer.parseInt(secondMatcher.group(SEGMENT_FILE_NAME_INDEX_CAPTURING_GROUP_NAME));

    return Integer.compare(firstIndex, secondIndex);
  };


}
