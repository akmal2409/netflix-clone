package com.akmal2409.github.netflix.media.domain.configuration;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Set of configuration properties for S3 media bucket management (movies and series)
 * Common prefix path: app.media-s3
 */
@Configuration
@ConfigurationProperties(prefix = "app.media-s3")
@Getter
@Setter
@Validated
public class MediaS3ConfigurationProperties {

  /**
   * AWS S3 host to connect to
   */
  @NotEmpty(message = "AWS S3 media host is missing")
  private String host;

}
