package io.github.akmal2409.job;

public class InvalidManifestException extends RuntimeException {

  public InvalidManifestException(String key, Object value, String reason) {
    super(String.format("Invalid manifest value for key %s with value %s. Reason: %s",
        key, value, reason));
  }

}
