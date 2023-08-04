package com.github.akmal2409.netflix.videoslicer.job;

import java.util.UUID;

/**
 * Job manifest for preprocessing job that contains the source file location,
 * segment durations, outputs etc.
 */
public record PreprocessingManifest(
    UUID jobId,
    String sourceBucket, // input (source) s3 bucket
    String sourceFileKey, // input file key in s3
    String outputBucket, // output s3 bucket
    String outputFileKeyPrefix, // there can be many files, prefix will group them
    int segmentDurationMs
) {

}
