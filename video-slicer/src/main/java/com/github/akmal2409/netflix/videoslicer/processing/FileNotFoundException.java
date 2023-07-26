package com.github.akmal2409.netflix.videoslicer.processing;

public class FileNotFoundException extends RuntimeException {

  public FileNotFoundException(String videoFile) {
    super(String.format("File/directory %s is missing", videoFile));
  }
}
