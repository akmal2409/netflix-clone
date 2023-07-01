package com.akmal2409.github.netflix.media.domain.service.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.akmal2409.github.netflix.media.domain.dto.internal.VideoContentUploadRequest;
import com.akmal2409.github.netflix.media.domain.model.VideoContent;
import com.akmal2409.github.netflix.media.extensions.infrastructure.LocalStackExtension;
import com.akmal2409.github.netflix.media.extensions.infrastructure.PostgresExtension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ExtendWith(PostgresExtension.class)
@Import(VideoUploadServiceIT.Config.class)
class VideoUploadServiceIT {

  @TestConfiguration
  public static class Config {

    @Bean
    public RestTemplate restTemplate() {
      return new RestTemplate();
    }
  }


  static final VideoContent testContent = new VideoContent() {
    @Override
    public String bucketName() {
      return "test-bucket";
    }

    @Override
    public String objectKey() {
      return "testkey";
    }
  };


  @RegisterExtension
  static LocalStackExtension localStackExtension = new LocalStackExtension();

  @Autowired
  VideoUploadService uploadService;

  @Autowired
  RestTemplate restTemplate;

  @BeforeEach
  void setup() throws IOException, InterruptedException {
    localStackExtension.getContainer().execInContainer("awslocal", "s3", "mb", "s3://" + testContent.bucketName());
  }

  @AfterEach
  void tearDown() throws IOException, InterruptedException {
    localStackExtension.getContainer().execInContainer("awslocal", "s3", "rb", "s3://" + testContent.bucketName());
  }

  @Test
  @DisplayName("Generates presigned URL with validity if upload request is valid")
  void generatesPresignedUrlWithValidityWhenValidRequest() throws URISyntaxException {
    Instant validityLowerBound = Instant.now().plus(Duration.ofHours(2)); // everything after can have a margin of error

    final var validUploadRequest = new VideoContentUploadRequest(10, "video/mp4");

    final PreSignedURL preSignedUrl = uploadService.getSignedUploadUrl(validUploadRequest, testContent);

    assertThat(preSignedUrl.validUntil()).isAfter(validityLowerBound);
    assertThat(preSignedUrl.url()).isNotNull();

    // validate presigned URL
    byte[] content = new byte[10];
    HttpEntity<byte[]> requestEntity = new HttpEntity<>(content,
        new MultiValueMapAdapter<>(Map.of(
            HttpHeaders.CONTENT_TYPE, List.of("video/mp4"),
            HttpHeaders.CONTENT_LENGTH, List.of("10")
        )));


    HttpStatusCode code = restTemplate.exchange(preSignedUrl.url().toURI(), HttpMethod.PUT, requestEntity, Object.class)
        .getStatusCode();

    assertThat(code.is2xxSuccessful()).isTrue();
  }
}
