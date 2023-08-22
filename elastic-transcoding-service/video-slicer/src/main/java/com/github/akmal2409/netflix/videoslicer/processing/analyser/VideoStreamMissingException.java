package com.github.akmal2409.netflix.videoslicer.processing.analyser;

import java.nio.file.Path;

public class VideoStreamMissingException extends VideoAnalysisException {

  protected VideoStreamMissingException(Path sourceFile) {
    super(String.format("Video file %s has a missing video stream", sourceFile), sourceFile);
  }
}
