package com.github.akmal2409.netflix.videoslicer.processing.analyser;

import com.github.akmal2409.netflix.videoslicer.coordinator.frame.Segment;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Predicate;

public interface VideoAnalyser {

  /**
   * Analyses source video and produce an index containing
   * meta information of the video file.
   *
   * @param source file
   */
  VideoIndex analyse(Path source);

  /**
   * Analyses segments in the given directory whose names match the given segmentPattern
   *
   * @param segmentDirectory where segments are located, can be mixed with other files
   * @param segmentFileMatcher matcher of segment files in the directory
   * @param comparator used for sorting segments based on their order of appearance.
   * @return indexed segments
   */
  Segment[] analyseSegments(Path segmentDirectory, Predicate<Path> segmentFileMatcher, Comparator<Path> comparator);
}
