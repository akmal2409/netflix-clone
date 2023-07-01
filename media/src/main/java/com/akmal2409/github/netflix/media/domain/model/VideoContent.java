package com.akmal2409.github.netflix.media.domain.model;

import java.util.Collections;
import java.util.Map;

/**
 * Common interface for classes that support uploading media to S3 like storage.
 */
public interface VideoContent {

  /**
   * Bucket name where the media is stored
   */
  String bucketName();

  /**
   * File key within the bucket (including prefix and excluding file extension)
   */
  String objectKey();

  /**
   * S3 user defined object metadata like content type and length
   * <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/UsingMetadata.html">See possible user defined metadata</a>
   */
  default Map<String, String> metadata() {
    return Collections.emptyMap();
  }
}
