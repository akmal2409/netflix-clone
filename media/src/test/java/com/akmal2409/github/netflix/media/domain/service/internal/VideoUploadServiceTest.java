package com.akmal2409.github.netflix.media.domain.service.internal;

import static org.assertj.core.api.Assertions.*;

import com.akmal2409.github.netflix.media.domain.dto.internal.VideoContentUploadRequest;
import com.akmal2409.github.netflix.media.domain.exception.UnsupportedVideoContentTypeException;
import com.akmal2409.github.netflix.media.domain.exception.VideoUploadFailedException;
import com.akmal2409.github.netflix.media.domain.model.VideoContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class VideoUploadServiceTest {

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

  @InjectMocks
  VideoUploadService service;

  @Test
  @DisplayName("getSignedUploadUrl() throws exception when invalid content type passed")
  void throwsExceptionWhenInvalidContentTypePassed() {
    final var invalidUpload = new VideoContentUploadRequest(10, "video/mp3");

    assertThatThrownBy(() -> service.getSignedUploadUrl(invalidUpload, testContent))
        .isInstanceOf(UnsupportedVideoContentTypeException.class);
  }

  @Test
  @DisplayName("getSignedUploadUrl() throws exception when negative and 0 content length is passed")
  void throwsExceptionWhenInvalidContentLengthIsPassed() {
    final var firstInvalidUpload = new VideoContentUploadRequest(-10, "video/mp4");
    final var secondInvalidUpload = new VideoContentUploadRequest(0, "video/mp4");

    assertThatThrownBy(() -> service.getSignedUploadUrl(firstInvalidUpload, testContent))
        .isInstanceOf(VideoUploadFailedException.class);

    assertThatThrownBy(() -> service.getSignedUploadUrl(secondInvalidUpload, testContent))
        .isInstanceOf(VideoUploadFailedException.class);
  }
}
