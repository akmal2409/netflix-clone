package com.github.akmal2409.netflix.videoslicer.config.loader;

public class ConfigurationLoadingException extends RuntimeException {

  public ConfigurationLoadingException(String message) {
    super(message);
  }

  public ConfigurationLoadingException(String message, Throwable cause) {
    super(message, cause);
  }
}
