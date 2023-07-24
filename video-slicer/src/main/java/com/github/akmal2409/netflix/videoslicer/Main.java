package com.github.akmal2409.netflix.videoslicer;

import com.github.akmal2409.netflix.videoslicer.api.VideoSlicer;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;

public class Main {

  public static void main(String[] args) throws IOException {
    FFmpeg ffmpeg = new FFmpeg("/opt/homebrew/bin/ffmpeg");
    FFprobe ffprobe = new FFprobe("/opt/homebrew/bin/ffprobe");
    FFmpegExecutor ffmpegExecutor = new FFmpegExecutor(ffmpeg, ffprobe);

    VideoSlicer slicer = FFmpegVideoSlicer.withExecutor(ffmpegExecutor);

    slicer.slice(Path.of("/Users/akmalalikhujaev/Personal/Dev/netflix-clone/video-slicer/video.mp4"), Path.of("/Users/akmalalikhujaev/Personal/Dev/netflix-clone-sample-files/tmp-playground"), Duration.ofSeconds(5),
        "segment-%03d.mp4");
  }

}
