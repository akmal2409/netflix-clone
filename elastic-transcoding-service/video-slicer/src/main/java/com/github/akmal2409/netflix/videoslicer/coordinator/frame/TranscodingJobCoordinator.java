package com.github.akmal2409.netflix.videoslicer.coordinator.frame;

import com.github.akmal2409.netflix.videoslicer.coordinator.util.ArrayUtils;
import java.util.Arrays;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranscodingJobCoordinator {

  private static final Logger log = LoggerFactory.getLogger(TranscodingJobCoordinator.class);

  private static final Comparator<Segment> SEGMENT_BY_START_FRAME_COMPARATOR = Comparator.comparingInt(Segment::startFrame);
  private static final Comparator<Segment> SEGMENT_BY_END_FRAME_COMPARATOR = Comparator.comparingInt(Segment::endFrame);

  public OverlappingSegmentTranscodingJob[] buildJobs(TranscodingConfiguration config,
      Segment[] segments) {
    Arrays.sort(segments, SEGMENT_BY_START_FRAME_COMPARATOR);

    final int targetSegmentCount = (int) Math.ceil(config.totalFrames()/(double) config.gopSize());

    final var jobs = new OverlappingSegmentTranscodingJob[targetSegmentCount];

    int segmentStart = 0;
    int segmentEnd = config.gopSize() - 1; // [start, end] form 1 Group of Pictures

    int coveringSetStart; // overlapping segment start
    int coveringSetEnd; // overlapping segment end
    Segment target; // target segment for binary search comparison

    for (int segmentIndex = 0; segmentIndex < targetSegmentCount; segmentIndex++) {
      // find set S = {s_i, s_(i+1),..., s_(i+k)} such that:
      // s_i.startFrame is the largest frame <= segmentStart
      // and s_(i+k).endFrame is the smallest frame >= segmentEnd
      target = new Segment(null, -1, segmentStart, segmentEnd);

      coveringSetStart = ArrayUtils.binarySearchFloor(segments,
          target, SEGMENT_BY_START_FRAME_COMPARATOR, 0, segments.length);

      coveringSetEnd = ArrayUtils.binarySearchCeil(segments,
          target, SEGMENT_BY_END_FRAME_COMPARATOR, 0, segments.length);

      log.debug(
          "[SEGMENT {}] overlapping_segment_interval=[{}, {}] frames=[{}, {}] skip={} frames",
          segmentIndex, coveringSetStart, coveringSetEnd,
          segmentStart, segmentEnd, segmentStart - segments[coveringSetStart].startFrame()
      );

      jobs[segmentIndex] = new OverlappingSegmentTranscodingJob(
          segmentIndex, segmentStart, segmentEnd, coveringSetStart, coveringSetEnd,
          segmentStart - segments[coveringSetStart].startFrame()
      );

      segmentStart = segmentEnd + 1;

      if (segmentIndex == targetSegmentCount - 2 && (config.totalFrames() % config.gopSize() != 0)) {
        segmentEnd += (config.totalFrames() % config.gopSize()); // only last segment can have frames less than GOP
      } else {
        segmentEnd += config.gopSize();
      }
    }

    return jobs;
   }
}
