package com.akmal2409.github.netflix.media.domain.configuration;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.media-upload")
@Getter
@Setter
public class MediaUploadConfigurationProperties {

  /**
   * Validity of the pre-signed URL to upload media content to S3.
   * Default: 1h
   * Can be specified as a {@link Duration}.
   */
  private Duration preSignedUrlValidity = Duration.ofHours(1);
}
