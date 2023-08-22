package com.github.akmal2409.netflix.videoslicer.coordinator.frame;

public record Segment(
    String fileName,
    int index,
    int startFrame,
    int endFrame // inclusive
) {

}
