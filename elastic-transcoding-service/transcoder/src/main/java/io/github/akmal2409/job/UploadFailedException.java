package io.github.akmal2409.job;

public class UploadFailedException extends RuntimeException {

  public UploadFailedException(String message) {
    super(message);
  }

  public UploadFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
