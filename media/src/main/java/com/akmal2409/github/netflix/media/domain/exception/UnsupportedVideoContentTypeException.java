package com.akmal2409.github.netflix.media.domain.exception;

public class UnsupportedVideoContentTypeException extends RuntimeException{

  public UnsupportedVideoContentTypeException(String provided, Iterable<String> supported) {
    super(String.format("Unsupported contentType provided %s. Supported types %s",
        provided, String.join(", ", supported)));
  }

}
