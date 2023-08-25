package io.github.akmal2409.job.audio;

import java.util.UUID;

/**
 * Represents transcoding job manifest for a single audio file to single transcoded audio file conversions.
 *
 * @param jobId id of the transcoding ob
 * @param sourceBucket S3 bucket where the audio file is located
 * @param sourceKeyPrefix audio file key prefix
 * @param fileName audio file name
 * @param targetBitRate target audio bit rate (in bits)
 * @param codec target codec to use
 * @param outputBucket where to store the transcoded audio
 * @param outputKeyPrefix the key to use for the transcoded audio
 */
public record AudioTranscodingJobManifest(
    UUID jobId,
    String sourceBucket,
    String sourceKeyPrefix,
    String fileName,
    int targetBitRate,
    String codec,

    String outputBucket,
    String outputKeyPrefix
) {

}
