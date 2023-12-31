package com.github.akmal2409.netflix.videoslicer.processing;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.akmal2409.netflix.videoslicer.config.EnvironmentVariables;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FFmpegMediaExtractorIT {
  public static final String TEST_15S_MP4 = "test-15s.mp4";

  public static FFmpeg ffmpeg;
  public static FFprobe ffprobe;

  static {
    try {
      System.out.println(System.getenv(EnvironmentVariables.FFMPEG_PATH));
      ffmpeg = new FFmpeg(System.getenv(EnvironmentVariables.FFMPEG_PATH));
      ffprobe = new FFprobe(System.getenv(EnvironmentVariables.FFPROBE_PATH));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("Should slice input video into chunks of 5 seconds")
  void shouldSliceVideoIntoSegments() throws IOException, URISyntaxException {
    final Path tmpDir = Files.createTempDirectory(null);
    URL videoUrl = this.getClass().getClassLoader().getResource(TEST_15S_MP4);
    Path testVideoPath = Path.of(videoUrl.toURI());
    Pattern segmentRegex = Pattern.compile("test-\\d{3}.mp4");

    MediaExtractor mediaExtractor = FFmpegMediaExtractor.withExecutor(new FFmpegExecutor(ffmpeg, ffprobe));

    mediaExtractor.slice(testVideoPath, tmpDir, Duration.ofSeconds(5), "test-%03d.mp4");


    try (Stream<Path> fileStream = Files.list(tmpDir)) {
      assertThat(fileStream.count()).isEqualTo(2);
    }

    try (Stream<Path> fileStream = Files.list(tmpDir)) {
      assertThat(fileStream.allMatch(file -> segmentRegex.matcher(file.getFileName().toString()).matches()))
          .isTrue();
    }
  }

  @Test
  @DisplayName("Should extract audio and package as mp4 container file")
  void shouldExtractAudio() throws URISyntaxException, IOException {
    URL videoUrl = this.getClass().getClassLoader().getResource(TEST_15S_MP4);
    Path testVideoPath = Path.of(videoUrl.toURI());
    Path audioPath = Files.createTempDirectory(null).resolve("audio.m4a");

    MediaExtractor mediaExtractor = FFmpegMediaExtractor.withExecutor(new FFmpegExecutor(ffmpeg, ffprobe));

    mediaExtractor.extractAudio(testVideoPath, audioPath);

    assertThat(Files.exists(audioPath)).isTrue();
  }
}
