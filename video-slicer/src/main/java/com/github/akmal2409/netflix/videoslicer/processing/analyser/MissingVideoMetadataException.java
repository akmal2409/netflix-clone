package com.github.akmal2409.netflix.videoslicer.processing.analyser;

import java.nio.file.Path;

public class MissingVideoMetadataException extends VideoAnalysisException {

  private static final String MSG_FORMAT = "Video file %s is missing metadata: %s";

  protected MissingVideoMetadataException(String metaDataName, Path sourceFile) {
    super(String.format(MSG_FORMAT, sourceFile.toString(), metaDataName), sourceFile);
  }

}
