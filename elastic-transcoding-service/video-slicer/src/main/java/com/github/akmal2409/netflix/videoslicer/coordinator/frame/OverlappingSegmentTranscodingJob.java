package com.github.akmal2409.netflix.videoslicer.coordinator.frame;

/**
 * Transcoding job representation that has a range of segments required to transcode 1 segment.
 * When splitting the video initially we might not get exact segments due to I-Frame positions, therefore,
 * we  will need to find overlapping segments to cut just right to make a segment of equal size
 * @param segmentIndex order of the segment.
 * @param startFrame of the assembled segment
 * @param endFrame of the assembled segment
 * @param overlappingSegmentStart segment who contains {@code startFrame} between its start and end frames. i.e. Overlapping segment
 * @param overlappingSegmentEnd segment who contains {@code endFrame} between its start and end frames.
 * @param skipFirstSegmentFrames number of frames to skip in the first segment. E.g. if the first segment contains more than 1 desired segments
 */
public record OverlappingSegmentTranscodingJob(
    int segmentIndex,
    int startFrame,
    int endFrame,
    int overlappingSegmentStart,
    int overlappingSegmentEnd,
    int skipFirstSegmentFrames // number of frames to ski
) {

}
