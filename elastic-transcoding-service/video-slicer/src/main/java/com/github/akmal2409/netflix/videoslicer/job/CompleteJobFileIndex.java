package com.github.akmal2409.netflix.videoslicer.job;

import java.util.List;
import java.util.UUID;

public record CompleteJobFileIndex(
    UUID jobId,
    int segmentDurationMs,
    String audioFileName,
    List<Segment> segments
) {

  public static record Segment(
      int index,
      String name,
      long startTimeMs,
      long endTimeMs
  ) {}
}
