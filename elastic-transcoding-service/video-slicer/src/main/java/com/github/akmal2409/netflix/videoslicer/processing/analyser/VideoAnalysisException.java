package com.github.akmal2409.netflix.videoslicer.processing.analyser;

import java.nio.file.Path;

public class VideoAnalysisException extends RuntimeException {

  private final Path sourceFile;

  protected VideoAnalysisException(String message, Path sourceFile) {
    super(message);
    this.sourceFile = sourceFile;
  }

  protected VideoAnalysisException(String message, Throwable cause, Path sourceFile) {
    super(message, cause);
    this.sourceFile = sourceFile;
  }
}
