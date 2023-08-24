package io.github.akmal2409.job;

import java.nio.file.Path;

/**
 * Record class represents a transcoding task converting a single video into multiple bit rates
 * and resolutions
 *
 * @param out output file name
 * @param width output video width in pixels
 * @param height output video height in pixels
 * @param bitRate output video bit rate (amount of information in a second)
 */
public record VideoQualityTranscodingTask(
    Path out,
    int width,
    int height,
    int bitRate
) {

}
