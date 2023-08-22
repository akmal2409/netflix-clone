package com.github.akmal2409.netflix.videoslicer.job;

import java.nio.file.Path;

public record JobVideoSource(
    Path filePath,
    String fileName,
    long contentLength
) {

}
