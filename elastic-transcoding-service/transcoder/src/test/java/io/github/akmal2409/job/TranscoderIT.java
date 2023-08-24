package io.github.akmal2409.job;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TranscoderIT {

  FFmpegExecutor executor = new FFmpegExecutor(new FFmpeg(), new net.bramp.ffmpeg.FFprobe());
  final FFprobe ffprobe = FFprobe.atPath();
  final Path testVideoPath = Path.of(
      this.getClass().getClassLoader().getResource("test-15s-only-video.mp4").toURI());

  final int testVideoWidth = 640;
  final int testVideoHeight = 360;
  final int testVideoBitRate = 269;

  final Path testVideoWithAudioPath = Path.of(
      this.getClass().getClassLoader().getResource("test-15s.mp4").toURI());

  Transcoder transcoder = new Transcoder(executor);

  TranscoderIT() throws URISyntaxException, IOException {
  }

  @Test
  @DisplayName("Skips 10 frames from a video")
  void skipKFramesMethodSkips10Frames() throws IOException {
    final var output = Files.createTempDirectory(null)
                           .resolve("out.mp4");

    transcoder.skipKFrames(testVideoPath, output, 10);

    final var expectedFrameCount = countFrames(testVideoPath) - 10;

    assertThat(countFrames(output))
        .isEqualTo(expectedFrameCount);
  }


  @Test
  @DisplayName("Joins 2 videos together")
  void joinTwoVideos() throws IOException {
    final Path[] paths = {testVideoPath, testVideoPath};
    final var finalDuration = getDuration(paths[0]) + getDuration(paths[1]);
    final Path out = Path.of(System.getProperty("java.io.tmpdir"), UUID.randomUUID() + ".mp4");

    transcoder.joinVideosWithGopSize(paths, out, 20);

    final float actualDuration = getDuration(out);

    assertThat(actualDuration)
        .isEqualTo(finalDuration, Offset.offset(0.1f));
  }


  @Test
  @DisplayName("Extracts first segment")
  void extractsFirstSegment() throws IOException {
    final Path video = testVideoPath;
    final Path out = Files.createTempDirectory(null).resolve("out.mp4");
    final var segmentDuration = 10;

    transcoder.extractFirstSegment(video, out, segmentDuration);

    assertThat(getDuration(out))
        .isEqualTo((float)segmentDuration, Offset.offset(3f));
  }


  @Test
  @DisplayName("Transcodes to different quality")
  void transcodesToMultipleQualities() throws IOException {
    final Path video = testVideoPath;
    final Path out = Files.createTempDirectory(null).resolve("test.mp4");

    final var expectedWidth = 320;
    final var expectedHeight = 320;
    final var expectedBitRate = 100;

    final VideoQualityTranscodingTask task = new VideoQualityTranscodingTask(out, 320, 320, 100);

    transcoder.transcodeToMultipleQualities(video, task);

    final int[] actualResolution = getResolution(out);
    final float actualBitRate = getBitRate(out);

    assertThat(actualResolution[0]).isEqualTo(expectedWidth);
    assertThat(actualResolution[1]).isEqualTo(expectedHeight);

    assertThat(actualBitRate).isEqualTo( expectedBitRate, Offset.offset(10f));

  }


  private int[] getResolution(Path video) {
    final var resolution = new int[2];

    final FFprobeResult result = ffprobe.setInput(video)
        .setShowEntries("format:stream")
        .execute();

    resolution[0] = result.getStreams().get(0).getWidth();
    resolution[1] = result.getStreams().get(0).getHeight();

    return resolution;
  }

  private float getBitRate(Path video) {
    return ffprobe.setInput(video)
               .setShowEntries("stream")
               .execute()
               .getStreams().get(0)
               .getProbeData().getInteger("bit_rate")/1000f;
  }

  private float getDuration(Path video) {
    return ffprobe.setInput(video)
               .setShowEntries("format")
               .execute()
               .getFormat().getDuration();
  }

  private int countFrames(Path video) {
    FFprobeResult result = ffprobe.setInput(video)
                               .setCountFrames(true)
                               .setShowEntries("stream=nb_read_frames")
                               .setSelectStreams("v:0")
                               .execute();

    return result.getStreams().get(0).getProbeData().getInteger("nb_read_frames");
  }
}
