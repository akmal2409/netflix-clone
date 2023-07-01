package com.akmal2409.github.netflix.media.domain.exception;

public class VideoUploadFailedException extends RuntimeException {

  public VideoUploadFailedException(String message) {
    super(message);
  }

  public VideoUploadFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
