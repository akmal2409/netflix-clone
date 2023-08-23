package io.github.akmal2409.job;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * A transcoding job manifest that the worker decodes from the queue.
 *
 * @param sourceBucket S3 bucket where the segments are located
 * @param sourceKeyPrefix S3 common file key prefix
 * @param outputBucket S3 output bucket for transcoded media
 * @param outputKeyPrefix S3 common file key prefix for output files
 * @param segmentFileNames file names of the overlapping segments that are going to be encoded
 * @param skipFirstFrames number of frames to skip in the first segment to align them
 * @param gopSize Group of Pictures size of a segment (desired one)
 */
public record TranscodingJobManifest(
    UUID jobId,
    String sourceBucket,
    String sourceKeyPrefix,
    String outputBucket,
    String outputKeyPrefix,
    int segmentIndex,
    int targetSegmentDurationSeconds,
    String[] segmentFileNames,
    int skipFirstFrames,
    int gopSize,
    VideoQuality[] outputQualities, // array of bitrates of videos that will be generated,
    VideoQuality originalQuality
) {

  public static record VideoQuality(
      int width,
      int height,
      int bitRate
  ) {

  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TranscodingJobManifest that = (TranscodingJobManifest) o;

    if (skipFirstFrames != that.skipFirstFrames) {
      return false;
    }
    if (gopSize != that.gopSize) {
      return false;
    }
    if (!Objects.equals(sourceBucket, that.sourceBucket)) {
      return false;
    }
    if (!Objects.equals(sourceKeyPrefix, that.sourceKeyPrefix)) {
      return false;
    }
    if (!Objects.equals(outputBucket, that.outputBucket)) {
      return false;
    }
    if (!Objects.equals(outputKeyPrefix, that.outputKeyPrefix)) {
      return false;
    }
    // Probably incorrect - comparing Object[] arrays with Arrays.equals
    return Arrays.equals(segmentFileNames, that.segmentFileNames);
  }

  @Override
  public int hashCode() {
    int result = sourceBucket != null ? sourceBucket.hashCode() : 0;
    result = 31 * result + (sourceKeyPrefix != null ? sourceKeyPrefix.hashCode() : 0);
    result = 31 * result + (outputBucket != null ? outputBucket.hashCode() : 0);
    result = 31 * result + (outputKeyPrefix != null ? outputKeyPrefix.hashCode() : 0);
    result = 31 * result + Arrays.hashCode(segmentFileNames);
    result = 31 * result + skipFirstFrames;
    result = 31 * result + gopSize;
    return result;
  }

  @Override
  public String toString() {
    return "TranscodingJobManifest{" +
               "sourceBucket='" + sourceBucket + '\'' +
               ", sourceKeyPrefix='" + sourceKeyPrefix + '\'' +
               ", outputBucket='" + outputBucket + '\'' +
               ", outputKeyPrefix='" + outputKeyPrefix + '\'' +
               ", segmentFileNames=" + Arrays.toString(segmentFileNames) +
               ", skipFirstFrames=" + skipFirstFrames +
               ", gopSize=" + gopSize +
               '}';
  }
}
