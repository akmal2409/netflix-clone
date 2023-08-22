package com.github.akmal2409.netflix.videoslicer.coordinator.frame;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TranscodingJobCoordinatorTest {

  // no overlapping segments => jobs are equal to the input
  TranscodingJobCoordinator coordinator;

  @BeforeEach
  void setup() {
    this.coordinator = new TranscodingJobCoordinator();
  }

  @Test
  @DisplayName("buildJobs() creates jobs that are the same as input when no overlaps")
  void buildJobsCreatesSameJobsWhenNoOverlap() {
    final var config = new TranscodingConfiguration(5, 15);

    final Segment[] segments = {
        new Segment(0, 0, 4),
        new Segment(1, 5, 9),
        new Segment(2, 10, 14)
    };

    final OverlappingSegmentTranscodingJob[] expectedJobs = {
        new OverlappingSegmentTranscodingJob(0, 0, 4, 0, 0, 0),
        new OverlappingSegmentTranscodingJob(1, 5, 9, 1, 1, 0),
        new OverlappingSegmentTranscodingJob(2, 10, 14, 2, 2, 0)
    };


    final OverlappingSegmentTranscodingJob[] actualJobs = coordinator.buildJobs(config, segments);

    assertThat(actualJobs)
        .usingRecursiveFieldByFieldElementComparator()
        .isEqualTo(expectedJobs);
  }

  // first segment containing 1.5 of the actual segment, the other 0.5 => 2 jobs of equal lengths
  @Test
  @DisplayName("buildJobs() creates jobs when first one contains partial job of the second")
  void buildJobsSplitsFirstSegmentInto2AndMergesSecondHalf() {
    final var config = new TranscodingConfiguration(6, 12);

    final Segment[] segments = {
        new Segment(0, 0, 8),
        new Segment(1, 9, 11)
    };

    final OverlappingSegmentTranscodingJob[] expectedJobs = {
        new OverlappingSegmentTranscodingJob(0, 0, 5, 0, 0, 0),
        new OverlappingSegmentTranscodingJob(1, 6, 11, 0, 1, 6)
    };


    final OverlappingSegmentTranscodingJob[] actualJobs = coordinator.buildJobs(config, segments);

    assertThat(actualJobs)
        .usingRecursiveFieldByFieldElementComparator()
        .isEqualTo(expectedJobs);
  }

  // first segment 2 of the actual segment => 2 jobs [1,1]
  @Test
  @DisplayName("buildJobs() splits a segment into 2 when segment is too long")
  void buildJobsSplits1SegmentInto2Jobs() {
    final var config = new TranscodingConfiguration(5, 10);

    final Segment[] segments = {
        new Segment(0, 0, 9)
    };

    final OverlappingSegmentTranscodingJob[] expectedJobs = {
        new OverlappingSegmentTranscodingJob(0, 0, 4, 0, 0, 0),
        new OverlappingSegmentTranscodingJob(1, 5, 9, 0, 0, 5)
    };


    final OverlappingSegmentTranscodingJob[] actualJobs = coordinator.buildJobs(config, segments);

    assertThat(actualJobs)
        .usingRecursiveFieldByFieldElementComparator()
        .isEqualTo(expectedJobs);
  }

  // first: 1.2 second: 0.6 third: 1.2 => [1, 1, 1] i.e. middle segment overlaps with the previous and next
  @Test
  @DisplayName("buildJobs() creates jobs correctly when middle segment depends on the previous and next segment")
  void buildJobsCorrectlyWhenMiddleDependsOnPrevAndNext() {
    final var config = new TranscodingConfiguration(10, 30);

    final Segment[] segments = {
        new Segment(0, 0, 11),
        new Segment(1, 12, 17),
        new Segment(2, 18, 29)
    };

    final OverlappingSegmentTranscodingJob[] expectedJobs = {
        new OverlappingSegmentTranscodingJob(0, 0, 9, 0, 0, 0),
        new OverlappingSegmentTranscodingJob(1, 10, 19, 0, 2, 10),
        new OverlappingSegmentTranscodingJob(2, 20, 29, 2, 2, 2)
    };


    final OverlappingSegmentTranscodingJob[] actualJobs = coordinator.buildJobs(config, segments);

    assertThat(actualJobs)
        .usingRecursiveFieldByFieldElementComparator()
        .isEqualTo(expectedJobs);
  }
}
