package com.github.akmal2409.netflix.videoslicer;

import com.github.akmal2409.netflix.videoslicer.api.FileNotFoundException;
import com.github.akmal2409.netflix.videoslicer.api.SegmentationNotPossibleException;
import com.github.akmal2409.netflix.videoslicer.api.VideoSlicer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.jetbrains.annotations.NotNull;

public class FFmpegVideoSlicer implements VideoSlicer {

  private final FFmpegExecutor ffmpegExecutor;

  private FFmpegVideoSlicer(FFmpegExecutor ffmpegExecutor) {
    this.ffmpegExecutor = ffmpegExecutor;
  }

  public static FFmpegVideoSlicer withExecutor(FFmpegExecutor ffmpegExecutor) {
    return new FFmpegVideoSlicer(ffmpegExecutor);
  }

  @Override
  public void slice(@NotNull Path source, @NotNull Path outputDir,
      @NotNull Duration segmentDuration, @NotNull String segmentFilenamePattern) {
    if (!Files.exists(source)) {
      throw new FileNotFoundException(source.toString());
    }

    if (!Files.isDirectory(outputDir)) {
      throw new FileNotFoundException(outputDir.toString());
    }

    if (segmentDuration.isNegative() || segmentDuration.isZero()) {
      throw new SegmentationNotPossibleException("Segment duration provided is either negative or zero. Duration: " + segmentDuration.toString());
    }

    final FFmpegBuilder ffBuilder = new FFmpegBuilder()
                                        .setInput(source.toString())
                                        .overrideOutputFiles(true)


                                        .addOutput(outputDir.resolve(segmentFilenamePattern).toString())
                                        .setFormat("segment")
                                        .addExtraArgs("-segment_time", String.valueOf(segmentDuration.toSeconds()), "-reset_timestamps", "1")

                                        .disableAudio()
                                        .disableSubtitle()
                                        .setVideoCodec("copy")
                                        .done();

    this.ffmpegExecutor.createJob(ffBuilder).run();
  }
}
