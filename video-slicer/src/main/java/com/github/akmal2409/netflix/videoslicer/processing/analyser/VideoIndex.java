package com.github.akmal2409.netflix.videoslicer.processing.analyser;


/**
 * Index instance produced by video analysis {@link VideoAnalyser}
 */
public record VideoIndex(
    double frameRate,
    int totalFrames,
    int width,
    int height
) {

}
