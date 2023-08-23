package io.github.akmal2409.job;

public class TranscodingException extends RuntimeException {

  public TranscodingException(String message) {
    super(message);
  }

  public TranscodingException(String message, Throwable cause) {
    super(message, cause);
  }
}
