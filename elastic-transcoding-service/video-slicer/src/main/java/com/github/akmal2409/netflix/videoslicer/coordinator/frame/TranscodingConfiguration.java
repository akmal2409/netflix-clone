package com.github.akmal2409.netflix.videoslicer.coordinator.frame;

public record TranscodingConfiguration(
    int gopSize,
    int totalFrames
) {

}
