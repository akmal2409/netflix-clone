package io.github.akmal2409.job;


import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import io.github.akmal2409.utils.FileUtils;
import io.github.akmal2409.utils.StringUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The encoded segments are always encoded with h264, High profile and 4.0 level.
 * Original segment will always have h264, High 4:4:4 and 4.0 level
 */
public class Transcoder {

  private static final Logger log = LoggerFactory.getLogger(Transcoder.class);

  private final FFmpegExecutor ffmpegExecutor;

  public Transcoder(FFmpegExecutor ffmpegExecutor) {
    this.ffmpegExecutor = ffmpegExecutor;
  }

  /**
   * Method that skips {@code skipFrameCount} number of frames from the video. If the video has less
   * than that amount, it will skip all frames.
   * <p>
   * Known limitations: 1) It will not skip any frames if the video contains audio stream.
   *
   * @param sourceVideo    in which you want to cut
   * @param skipFrameCount number of frames to skip (must be greater than 0)
   */
  public void skipKFrames(@NotNull Path sourceVideo, @NotNull Path outputFile, int skipFrameCount) {
    if (skipFrameCount < 1) {
      throw new TranscodingException("skipFrameCount is smaller than 1 frame");
    }

    if (!Files.exists(sourceVideo)) {
      throw new TranscodingException("sourceVideo doesn't exist");
    }

    final var jobBuilder = new FFmpegBuilder()
                               .setInput(
                                   Objects.requireNonNull(sourceVideo, "Source video is missing")
                                       .toString())
                               .overrideOutputFiles(true)

                               .addOutput(
                                   Objects.requireNonNull(outputFile, "Output file path is missing")
                                       .toString())
                               .setVideoCodec("libx264")
                               .setConstantRateFactor(0)
                               .setVideoFilter(
                                   String.format("select='gte(n\\,%d)'", skipFrameCount))
                               .done();

    this.ffmpegExecutor.createJob(jobBuilder).run();
  }

  /**
   * Extracts approximately <code>segmentDurationSeconds</code> segment out of the video.
   * It cuts the video into chunks and extracts the first one. Intermediary files are stored in a temporary folder that is cleaned up afterwards.
   *
   * @param sourceVideo from where to extract the segment
   * @param outFile output file with extension
   * @param segmentDurationSeconds desired segment duration
   */
  public void extractFirstSegment(Path sourceVideo, Path outFile, int segmentDurationSeconds) {
    if (!Files.exists(sourceVideo)) {
      throw new TranscodingException("Source video doesn't exist");
    } else if (segmentDurationSeconds < 1) {
      throw new TranscodingException("Segment duration is less than 1 second.");
    }

    final var outputFileContainerExtension = FileUtils.getFileExtension(outFile.getFileName().toString());

    // cut it into n-segments and take the first one out.
    Path tmpDirectory = null;
    try {
      tmpDirectory = Files.createTempDirectory(null);

      final var jobBuilder = new FFmpegBuilder()
                                 .addInput(sourceVideo.toString())
                                 .addOutput(tmpDirectory.resolve("segment-%09d." + outputFileContainerExtension).toString())
                                 .setVideoCodec("copy")
                                 .setAudioCodec("copy")
                                 .addExtraArgs("-f", "segment",
                                     "-segment_time", String.valueOf(segmentDurationSeconds),
                                     "-reset_timestamps", "1")
                                 .done();

      ffmpegExecutor.createJob(jobBuilder).run();

      final var firstSegmentName = "segment-000000000." + outputFileContainerExtension;

      try {
        Files.createDirectories(outFile.getParent());
        Files.move(tmpDirectory.resolve(firstSegmentName), outFile, REPLACE_EXISTING);
      } catch (IOException e) {
        throw new TranscodingException("Could not move file to the output path " + outFile);
      }

    } catch (IOException e) {
      throw new TranscodingException("Could not create a temporary directory to store segments");
    } finally {
      if (tmpDirectory != null) {
        try {
          FileUtils.deleteDirectory(tmpDirectory);
        } catch (IOException e) {
          // will be cleaned up after the restart anyways
        }
      }
    }
  }

  /**
   * Given at least 1 {@link VideoQualityTranscodingTask} transcodes the source video into the desired resolution and bitrate, while trying to
   * preserve the quality by using a 2-pass job.
   *
   * @param source video file that needs to be transcoded.
   * @param qualities that you want your video to be available in.
   */
  public void transcodeToMultipleQualities(Path source, VideoQualityTranscodingTask... qualities) {
    if (qualities == null || qualities.length == 0) {
      throw new TranscodingException("At least 1 video quality with an output should be provided");
    }

    if (!Files.exists(source)) {
      throw new TranscodingException("Source file doesn't exist");
    }

    final var jobBuilder = new FFmpegBuilder()
                               .addInput(source.toString());

    for (VideoQualityTranscodingTask task: qualities) {
      final var containerFormat = FileUtils.getFileExtension(task.out().getFileName().toString());

      jobBuilder.addOutput(task.out().toString())
          .setVideoCodec("libx264")
          .addExtraArgs("-profile:v", "high", "-level:v", "4")
          .addExtraArgs("-tune", "film")
          .setVideoBitRate(task.bitRate())
          .addExtraArgs("-b:v", String.valueOf(task.bitRate()))
          .setVideoFilter(String.format("scale=%d:%d", task.width(), task.height()))
          .addExtraArgs("-preset", "slow")

          .setFormat(containerFormat);
    }


    ffmpegExecutor.createTwoPassJob(jobBuilder).run();
  }

  /**
   * Joins a set of n-videos into 1 by and re-encodes it with a desired Group of Pictures (GOP)
   * size.
   *
   * @param videoPaths      the videos that must be joined
   * @param joinedVideoPath path to the final joined video
   * @param gopSize         distance in frames between I-Frames
   */
  public void joinVideosWithGopSize(Path[] videoPaths, Path joinedVideoPath, int gopSize) {
    if (videoPaths == null || videoPaths.length == 0) {
      throw new TranscodingException("Video paths not provided");
    }

    try {
      final var indexFile = Files.createTempFile(null, null);
      createFfmpegVideoIndexFile(indexFile, videoPaths);
      log.debug("Created index file {}", indexFile);

      final var jobBuilder = new FFmpegBuilder()
                                 .addExtraArgs("-f", "concat", "-safe", "0")
                                 .addInput(indexFile.toString())
                                 .addOutput(joinedVideoPath.toString())
                                 .setVideoCodec("libx264")
                                 .addExtraArgs("-x264opts",
                                     String.format("keyint=%d:min-keyint=%d:no-scenecut",
                                         gopSize, gopSize))
                                 .addExtraArgs("-profile:v", "high444", "-level:v", "4")
                                 .setConstantRateFactor(0)
                                 .done();

      ffmpegExecutor.createJob(jobBuilder).run();
      log.debug("Completed video stitching into a final file {}", joinedVideoPath);

    } catch (IOException e) {
      throw new TranscodingException("Exception occurred when generating index file", e);
    }
  }

  /**
   * Transcodes the source audio to target with the specified codec and bitRate.
   *
   * @param source audio file path
   * @param out output audio file
   * @param codec codec to use for encoding
   * @param bitRate bit rate in bits
   */
  public void transcodeAudio(@NotNull Path source, @NotNull Path out, @NotNull String codec, int bitRate) {
    if (bitRate < 1) {
      throw new TranscodingException("bitRate cannot be smaller than 1");
    }

    if (StringUtils.isEmpty(codec)) {
      throw new TranscodingException("Codec is required");
    }

    if (!Files.exists(source)) {
      throw new TranscodingException("Source file is not present");
    }

    try {
      Files.createDirectories(out.getParent());
    } catch (IOException e) {
      throw new TranscodingException("Could not create output directories", e);
    }

    final var jobBuilder = new FFmpegBuilder()
                               .addInput(source.toString())
                               .overrideOutputFiles(true)

                               .addOutput(out.toString())
                               .addExtraArgs("-vn", "-sn", "-dn")
                               .setAudioCodec(codec)
                               .setAudioBitRate(bitRate)
                               .done();

    ffmpegExecutor.createJob(jobBuilder).run();
  }

  /**
   * Creates an index file of form: file '<file_path/>
   * <p>
   * Useful when trying to concatenate or process multiple videos
   */
  private void createFfmpegVideoIndexFile(Path indexFile, Path... videoPaths) throws IOException {
    if (videoPaths == null || videoPaths.length == 0) {
      throw new TranscodingException("No video paths provided");
    }

    final var fileContents = new StringBuilder();

    for (Path videoPath : videoPaths) {
      fileContents.append(String.format("file '%s'%n", videoPath));
    }

    Files.writeString(indexFile, fileContents.toString(), StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
  }
}
