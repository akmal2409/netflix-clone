package com.akmal2409.github.netflix.media.domain.configuration;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class MediaS3Configuration {

  private final MediaS3ConfigurationProperties props;

  @Bean
  public AwsCredentialsProvider s3CredentialsProvider() {
    return DefaultCredentialsProvider.create();
  }

  @Bean
  public S3Client mediaS3Client(@Qualifier("s3CredentialsProvider") AwsCredentialsProvider credentialsProvider) {
    return S3Client.builder()
               .endpointOverride(URI.create(props.getHost()))
               .credentialsProvider(credentialsProvider)
               .forcePathStyle(true)
               .build();
  }

  @Bean
  public S3Presigner mediaS3Presigner(@Qualifier("s3CredentialsProvider") AwsCredentialsProvider credentialsProvider) {
    return S3Presigner.builder()
               .serviceConfiguration(S3Configuration.builder()
                                         .pathStyleAccessEnabled(true)
                                         .build())
               .endpointOverride(URI.create(props.getHost()))
               .credentialsProvider(credentialsProvider)
               .build();
  }
}
