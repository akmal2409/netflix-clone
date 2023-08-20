package com.github.akmal2409.netflix.videoslicer.processing.analyser;

import com.github.akmal2409.netflix.videoslicer.config.SegmentConstants;
import com.github.akmal2409.netflix.videoslicer.coordinator.frame.OverlappingSegmentTranscodingJob;
import com.github.akmal2409.netflix.videoslicer.coordinator.frame.Segment;
import com.github.akmal2409.netflix.videoslicer.coordinator.frame.TranscodingConfiguration;
import com.github.akmal2409.netflix.videoslicer.coordinator.frame.TranscodingJobCoordinator;
import com.github.akmal2409.netflix.videoslicer.processing.FFmpegVideoSlicer;
import com.github.akmal2409.netflix.videoslicer.processing.FileNotFoundException;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;

/**
 * Implementation of a {@link VideoAnalyser} interface that uses cli wrapper around FFprobe to
 * analyse the video and extract the exact frame rate along with number of frames and the duration.
 * <p>
 * Furthermore, it provides analysis capabilities for individual segments.
 */
public class FFprobeVideoAnalyser implements VideoAnalyser {

  private final FFprobe ffprobe;

  public FFprobeVideoAnalyser(FFprobe ffprobe) {
    this.ffprobe = ffprobe;
  }

  /**
   * Indexes the source video file and extracts two key components such as the frame-rate and the
   * frame count needed later for scheduling transcoding jobs. The method always selects the 0th
   * video stream so if the container format has more than 1 video stream, the method will take the
   * first one (at index 0)
   *
   * @param source file
   * @return video index.
   */
  @Override
  public VideoIndex analyse(Path source) {
    if (!Files.exists(source)) {
      throw new FileNotFoundException(source.toString());
    }

    try (final var inChannel = Files.newByteChannel(source,
        StandardOpenOption.READ)) {
      final FFprobeResult result = ffprobe.setInput(inChannel)
                                       .setSelectStreams("v:0")
                                       .setCountFrames(true)
                                       .setShowEntries("stream:format")
                                       .execute();

      validateProbeResult(result, 0, source);

      final double frameRate = result.getStreams().get(0)
                                   .getRFrameRate().doubleValue();
      final int totalFrames = result.getStreams().get(0)
                                  .getProbeData().getInteger("nb_read_frames");
      final int width = result.getStreams().get(0)
                            .getWidth();
      final int height = result.getStreams().get(0)
                             .getHeight();

      return new VideoIndex(frameRate, totalFrames, width, height);
    } catch (IOException e) {
      throw new VideoAnalysisException(
          String.format("Exception occurred when analysing the source file: %s", source),
          source
      );
    }
  }

  /**
   * Validates the result returned by FFprobe that checks whether the frame rate, number of frames
   * and the duration are present. Those are key elements required for both source video and
   * segment
   *
   * @param result      ffprobe result
   * @param streamIndex index of a video stream (if selecting only one stream the index will be 0)
   * @param source      video file on which the analysis was performed
   */
  private void validateProbeResult(FFprobeResult result, int streamIndex, Path source) {
    if (result.getStreams().isEmpty()) {
      throw new VideoStreamMissingException(source);
    } else if (result.getStreams().get(streamIndex).getRFrameRate() == null) {
      throw new MissingVideoMetadataException("r_frame_rate", source);
    } else if (result.getStreams().get(streamIndex).getProbeData().getInteger("nb_read_frames")
                   == null) {
      throw new MissingVideoMetadataException("nb_read_frames", source);
    }
  }

  @Override
  public Segment[] analyseSegments(Path segmentDirectory, Predicate<Path> segmentMatcher,
      Comparator<Path> segmentComparator) {

    try (final var fileStream = Files.list(segmentDirectory)
                                    .filter(segmentMatcher)
                                    .sorted(segmentComparator)) {

      int startFrame = 0;
      int index = 0;
      Path segmentPath;
      List<Segment> segments = new ArrayList<>();

      for (Iterator<Path> it = fileStream.iterator(); it.hasNext(); ) {
        segmentPath = it.next();

        segments.add(analyseSegment(segmentPath, index++, startFrame));
        startFrame = segments.get(segments.size() - 1).endFrame() + 1;
      }

      return segments.toArray(new Segment[0]);
    } catch (IOException e) {

    }

    return null;
  }

  private Segment analyseSegment(Path segmentPath, int index, int startFrame) {
    try (final var inChannel = Files.newByteChannel(segmentPath, StandardOpenOption.READ)) {
      final FFprobeResult result = ffprobe.setInput(inChannel)
                                       .setSelectStreams("v:0")
                                       .setCountFrames(true)
                                       .setShowEntries("stream")
                                       .execute();

      if (result.getStreams().isEmpty()) {
        throw new VideoStreamMissingException(segmentPath);
      }

      final int totalFrames = result.getStreams().get(0)
                                  .getProbeData().getInteger("nb_read_frames");

      return new Segment(segmentPath.getFileName().toString(), index, startFrame,
          startFrame + totalFrames - 1);
    } catch (IOException e) {
      throw new VideoAnalysisException(
          String.format("Exception occurred when analysing the source segment file: %s",
              segmentPath),
          segmentPath
      );
    }
  }


  public static void main(String[] args) throws IOException {
    final var analyser = new FFprobeVideoAnalyser(FFprobe.atPath());

    Path sourceVideo = Path.of("./video.mkv");
    Path segmentPath = Path.of("./segments");

    final VideoIndex index = analyser.analyse(sourceVideo);

    final var slicer = FFmpegVideoSlicer.withExecutor(new FFmpegExecutor(
        new FFmpeg(),
        new net.bramp.ffmpeg.FFprobe()
    ));

    slicer.slice(sourceVideo,
        segmentPath, Duration.ofSeconds(2),
        SegmentConstants.SEGMENT_FILE_NAME_PATTERN);

    int totalFrames = 0;

    final Segment[] segments = analyser.analyseSegments(segmentPath,
        (path) -> SegmentConstants.SEGMENT_FILE_NAME_REGEX_PATTERN.matcher(
            path.getFileName().toString()).matches(),
        (p1, p2) -> SegmentConstants.SEGMENT_FILE_NAME_COMPARATOR.compare(
            p1.getFileName().toString(), p2.getFileName().toString())
    );

    for (Segment segment : segments) {
      System.out.println(segment);
      totalFrames += (segment.endFrame() - segment.startFrame() + 1);
    }

    TranscodingJobCoordinator coordinator = new TranscodingJobCoordinator();

    final int segmentDurationSeconds = 5;
    final int gopSize = (int) Math.ceil(segmentDurationSeconds * index.frameRate());

    final OverlappingSegmentTranscodingJob[] jobs = coordinator.buildJobs(
        new TranscodingConfiguration(
            gopSize, index.totalFrames()
        ), segments);
    System.out.println("============= Meta =============");
    System.out.printf("Frame rate: %f%n", index.frameRate());
    System.out.printf("Total frames: %d%n", index.totalFrames());
    System.out.printf("Segment sum of frames: %d%n", totalFrames);
    System.out.printf("Segment duration: %ds%n", segmentDurationSeconds);
    System.out.printf("GOP size: %d%n", gopSize);

    System.out.println("============= Jobs =============");

    for (OverlappingSegmentTranscodingJob job : jobs) {
      System.out.println(job);
    }
  }
}
