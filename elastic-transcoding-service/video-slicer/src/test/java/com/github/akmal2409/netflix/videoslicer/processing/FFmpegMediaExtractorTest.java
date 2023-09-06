package com.github.akmal2409.netflix.videoslicer.processing;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import net.bramp.ffmpeg.FFmpegExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class FFmpegMediaExtractorTest {

  @Mock
  FFmpegExecutor executor;

  @InjectMocks
  FFmpegMediaExtractor slicer;

  @Test
  @DisplayName("Will throw File not found exception if nonexistent file requested")
  void nonExistentFileToSliceSupplied() throws IOException {
    Path nonExistentSource = Path.of(UUID.randomUUID().toString());
    Path out = Files.createTempDirectory(null);

    assertThatThrownBy(() -> slicer.slice(nonExistentSource, out,
        Duration.ofSeconds(5), "segment.mp4"))
        .isInstanceOf(FileNotFoundException.class);
  }

  @Test
  @DisplayName("Will throw File not found exception if nonexistent directory passed to slicer")
  void momExistentOutDirectoryPassedToSlice() throws IOException {
    Path nonExistentDir = Path.of(UUID.randomUUID().toString());
    Path source = Files.createTempFile(null, null);

    assertThatThrownBy(() -> slicer.slice(source, nonExistentDir,
        Duration.ofSeconds(5), "segment.mp4"))
        .isInstanceOf(FileNotFoundException.class);
  }

  @Test
  @DisplayName("Will throw File not found exception if non-directory passed as a directory output")
  void nonDirectoryPassedsOutputDir() throws IOException {
    Path outFile = Files.createTempFile(null, null);
    Path source = Files.createTempFile(null, null);

    assertThatThrownBy(() -> slicer.slice(source, outFile,
        Duration.ofSeconds(5), "segment.mp4"))
        .isInstanceOf(FileNotFoundException.class);
  }

  @Test
  @DisplayName("Will throw SegmentationNotPossibleException when duration is zero or negative")
  void negativeOrZeroDurationForSegmentation() throws IOException {
    Path out = Files.createTempDirectory(null);
    Path source = Files.createTempFile(null, null);

    assertThatThrownBy(() -> slicer.slice(source, out,
        Duration.ofSeconds(0), "segment.mp4"))
        .isInstanceOf(SegmentationNotPossibleException.class);

    assertThatThrownBy(() -> slicer.slice(source, out,
        Duration.ofSeconds(-1), "segment.mp4"))
        .isInstanceOf(SegmentationNotPossibleException.class);
  }


  @Test
  @DisplayName("Will throw FileNotFoundException when non existent video file passed to extractAudio")
  void throwsExceptionWhenNoVideoForAudioExtraction() {

    assertThatThrownBy(() -> slicer.extractAudio(Path.of(UUID.randomUUID().toString()),
        Path.of(UUID.randomUUID().toString())))
        .isInstanceOf(FileNotFoundException.class);
  }
}
