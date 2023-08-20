package com.github.akmal2409.netflix.videoslicer.processing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FFmpegVideoSlicer implements VideoSlicer {


  private static final Logger log = LoggerFactory.getLogger(FFmpegVideoSlicer.class);

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

    if (!Files.exists(outputDir)) {
      try {
        Files.createDirectories(outputDir);
      } catch (IOException e) {
        throw new OutputWriteException("Cannot create output directories", e);
      }
    } else if (!Files.isDirectory(outputDir)) {
      throw new FileNotFoundException(outputDir.toString());
    }

    if (segmentDuration.isNegative() || segmentDuration.isZero()) {
      throw new SegmentationNotPossibleException(
          "Segment duration provided is either negative or zero. Duration: "
              + segmentDuration);
    }

    log.debug(
        "message=Preparing to segment video in chunks with duration {}ms. Source: {} Output: {}/{}",
        segmentDuration.toMillis(), source, outputDir, segmentFilenamePattern);

    final FFmpegBuilder ffBuilder = new FFmpegBuilder()
                                        .setInput(source.toString())
                                        .overrideOutputFiles(true)

                                        .addOutput(
                                            outputDir.resolve(segmentFilenamePattern).toString())
                                        .setFormat("segment")
                                        .addExtraArgs("-segment_time",
                                            String.valueOf(segmentDuration.toSeconds()),
                                            "-reset_timestamps", "1")

                                        .disableAudio()
                                        .disableSubtitle()
                                        .setVideoCodec("copy")
                                        .done();

    this.ffmpegExecutor.createJob(ffBuilder).run();

    log.debug("message=Successfully finished segmenting video");
  }

  @Override
  public void extractAudio(Path source, Path out) {
    if (!Files.exists(source)) {
      throw new FileNotFoundException(source.toString());
    }

    if (!Files.exists(out)) {
      try {
        Files.createDirectories(out.getParent());
      } catch (IOException e) {
        throw new OutputWriteException("Cannot create output directories", e);
      }
    }

    log.debug("message=Starting extraction of audio from source: {} to output: {}",
        source, out);

    final FFmpegBuilder ffBuilder = new FFmpegBuilder()
                                        .setInput(source.toString())
                                        .overrideOutputFiles(true)

                                        .addOutput(out.toString())
                                        .disableVideo()
                                        .disableSubtitle()
                                        .setAudioCodec("copy")
                                        .done();

    this.ffmpegExecutor.createJob(ffBuilder).run();

    log.debug("message=Successfully extracted audio");
  }
}
