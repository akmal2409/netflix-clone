package io.github.akmal2409.job;


public record PackagingJob(
    int gopSize,
    double frameRate,
    String audioRelativePath

) {

  /**
   * Using 3 values of width, height and bitRate we can
   * @param width
   * @param height
   * @param bitRate
   */
  public static record MediaVariant(
      int width,
      int height,
      int bitRate, // in bits per second
  ) {}

  /**
   * Since all segments except for the last one hae a fixed Group Of Pictures size
   * then gopSize/frameRate rounded to 3 decimals (according to HLS) should be used as a
   * max segment duration
   * Max Segment Duration = ceil((gopSize / frameRate) * 1000) / 1000.0f
   */
  public float maximumSegmentDuration() {
    return ((float) Math.ceil((gopSize / frameRate) * 1_000)) / 1_000.0f;
  }
}
