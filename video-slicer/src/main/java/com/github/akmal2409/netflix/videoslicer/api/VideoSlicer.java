package com.github.akmal2409.netflix.videoslicer.api;

import java.nio.file.Path;
import java.time.Duration;

public interface VideoSlicer {

  /**
   * Slices given video {@code source} file into segments with duration
   * specified by {@code segmentDuration}. The segments might not be of exactly
   * segmentDuration, it depends on the implementation of the interface.
   *
   * @param source video file
   * @param outputDir directory to store segments
   * @param segmentDuration duration of each segment, the end result might not be exactly same as specified
   * @param segmentFilenamePattern template for the segment file name. For example, 'segment-%03d.mp4' will create segment-000.mp4, segment-001.mp4 etc
   */
  void slice(Path source, Path outputDir, Duration segmentDuration, String segmentFilenamePattern);
}
