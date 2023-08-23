package io.github.akmal2409.job;

public class DownloadFailedException extends RuntimeException {

  public DownloadFailedException(String message) {
    super(message);
  }

  public DownloadFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
