package com.github.akmal2409.netflix.videoslicer.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.akmal2409.netflix.videoslicer.config.SegmentConstants;
import com.github.akmal2409.netflix.videoslicer.coordinator.frame.Segment;
import com.github.akmal2409.netflix.videoslicer.job.ProcessedIndex.ProcessedSegment;
import com.github.akmal2409.netflix.videoslicer.job.exception.JobExecutionFailureException;
import com.github.akmal2409.netflix.videoslicer.processing.MediaExtractor;
import com.github.akmal2409.netflix.videoslicer.processing.analyser.VideoAnalyser;
import com.github.akmal2409.netflix.videoslicer.processing.analyser.VideoIndex;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobConsumer extends DefaultConsumer {

  private static final Logger log = LoggerFactory.getLogger(JobConsumer.class);

  private final MediaExtractor mediaExtractor;
  private final ObjectMapper objectMapper;
  private final S3Store s3Store;
  private final Executor executor;
  private final VideoAnalyser videoAnalyser;

  public JobConsumer(Channel channel, MediaExtractor mediaExtractor, ObjectMapper objectMapper,
      S3Store videoStore, Executor executor, VideoAnalyser videoAnalyser) {
    super(channel);
    this.mediaExtractor = mediaExtractor;
    this.objectMapper = objectMapper;
    this.s3Store = videoStore;
    this.executor = executor;
    this.videoAnalyser = videoAnalyser;
  }

  /**
   * The processing pipeline is simple. Firstly, when the message is received, we try to construct
   * and validate the job manifest that contains information about the source video file and output
   * bucket etc. Then, the pipeline is as following: 1) Download the source video file 2) Slice the
   * video into segments of size close to segmentDuration (cannot be exact due to I-Frame positions)
   * 3) Extract the audio from the video 4) Index the video files and create a JSON file containing
   * segments and their durations 5) Upload segments, audio and index.json to the output bucket.
   *
   * @param consumerTag the <i>consumer tag</i> associated with the consumer
   * @param envelope    packaging data for the message
   * @param properties  content header data for the message
   * @param body        the message body (opaque, client-specific byte array)
   * @throws IOException
   */
  @Override
  public void handleDelivery(String consumerTag,
      Envelope envelope, BasicProperties properties,
      byte[] body) throws IOException {
    final PreprocessingManifest manifest = objectMapper.readValue(body,
        PreprocessingManifest.class);

    log.info("message=Received preprocessing manifest;job_id={};consumer_tag={}",
        manifest.jobId(), consumerTag);

    if (!validateManifest(manifest)) {
      // for now just drop and reject
      // TODO: send it it another queue, where alert can be dispatched
      getChannel().basicAck(envelope.getDeliveryTag(), false);
    } else {
      final JobVideoSource videoSource = s3Store.downloadSource(manifest.jobId(),
          manifest.sourceBucket(), manifest.sourceFileKey());

      final var outputFileDirectory = videoSource.filePath().getParent().resolve("processed");

      // extracting frame rate and total number of frames in the video.
      final VideoIndex videoIndex = videoAnalyser.analyse(videoSource.filePath());

      final var audioFilePath = outputFileDirectory.resolve("audio.m4a");

      try {
        final var audioExtractionFuture = CompletableFuture.runAsync(() ->
                                                                         mediaExtractor.extractAudio(
                                                                             videoSource.filePath(),
                                                                             audioFilePath),
            executor);

        // while the audio task is running async, we can wait for segmentation to finish and then index all files
        final var segmentsPath = outputFileDirectory.resolve("segments");
        mediaExtractor.slice(
            videoSource.filePath(),
            segmentsPath,
            Duration.ofSeconds(manifest.segmentDurationSeconds()),
            SegmentConstants.SEGMENT_FILE_NAME_PATTERN);

        audioExtractionFuture.join();

        // Now we need to get start and end frame numbers of each segment
        final Segment[] segments = videoAnalyser.analyseSegments(segmentsPath,
            (path) -> SegmentConstants.SEGMENT_FILE_NAME_REGEX_PATTERN.matcher(
                path.getFileName().toString()).matches(),
            (p1, p2) -> SegmentConstants.SEGMENT_FILE_NAME_COMPARATOR.compare(
                p1.getFileName().toString(), p2.getFileName().toString())
        );

        final ProcessedIndex finalIndex = createCompleteFileIndex(videoIndex, segments,
            manifest, audioFilePath.getFileName()
                          .toString()); // index will be written as json file along the files in an output bucket under index.json

        final var indexFilePath = segmentsPath.getParent().resolve("index.json");

        try {
          writeIndexFile(indexFilePath, finalIndex);
        } catch (IOException e) {
          log.error("message=Failed to write index.json on disk;job_id={};consumer_tag={}",
              manifest.jobId(), consumerTag);

          throw new JobExecutionFailureException("Failed to write index file", manifest.jobId());
        }

        s3Store.uploadProcessedFiles(manifest.outputBucket(),
            manifest.outputFileKeyPrefix(), outputFileDirectory);

        getChannel().basicAck(envelope.getDeliveryTag(),
            false); // acknowledge it and complete execution
        
      } finally {
        cleanupFiles(videoSource.filePath().getParent());
      }


    }
  }

  /**
   * Removes all intermediary files that were required by the pipeline.
   * @param directory path where all the files and directories reside.
   */
  private void cleanupFiles(Path directory) throws IOException {
    Files.walkFileTree(directory, Set.of(), 10, new FileVisitor<>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
          throws IOException {
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        // since we delete only files on the way, we need to ensure that after we are done
        // visiting, we also delete the folder
        Files.delete(dir); // folder must be empty
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private void writeIndexFile(Path indexFilePath, ProcessedIndex finalIndex)
      throws IOException {
    final var indexJson = objectMapper.writeValueAsString(finalIndex);

    Files.writeString(indexFilePath, indexJson, StandardOpenOption.WRITE,
        StandardOpenOption.CREATE_NEW,
        StandardOpenOption.TRUNCATE_EXISTING);
  }


  private ProcessedIndex createCompleteFileIndex(VideoIndex videoIndex, Segment[] segments,
      PreprocessingManifest manifest, String audioFileName) {
    final ProcessedSegment[] mappedSegments = Arrays.stream(segments)
                                                  .map(
                                                      segment -> new ProcessedSegment(
                                                          segment.fileName(),
                                                          segment.index(),
                                                          segment.startFrame(),
                                                          segment.endFrame()))
                                                  .toArray(
                                                      ProcessedSegment[]::new);

    // The distance between I-Frames (IDR), recommended to be equal for ABR streaming
    final int gopSize = (int) Math.ceil(manifest.segmentDurationSeconds() * videoIndex.frameRate());
    return new ProcessedIndex(
        audioFileName,
        videoIndex.frameRate(),
        videoIndex.totalFrames(),
        videoIndex.width(),
        videoIndex.height(),
        gopSize,
        manifest.segmentDurationSeconds(),
        mappedSegments
    );
  }

  private boolean validateManifest(PreprocessingManifest manifest) {
    StringBuilder invalidProps = new StringBuilder();

    if (manifest.jobId() == null) {
      invalidProps.append("jobId is missing, ");
    }

    if (StringUtils.isEmpty(manifest.sourceBucket())) {
      invalidProps.append("sourceBucket is missing, ");
    }

    if (StringUtils.isEmpty(manifest.sourceFileKey())) {
      invalidProps.append("sourceFileKey is missing, ");
    }

    if (StringUtils.isEmpty(manifest.outputBucket())) {
      invalidProps.append("outputBucket is missing, ");
    }

    if (StringUtils.isEmpty(manifest.outputFileKeyPrefix())) {
      invalidProps.append("outputFileKeyPrefix is missing, ");
    }

    if (manifest.segmentDurationSeconds() < 1) {
      invalidProps.append("segmentDurationSeconds is less than 1, ");
    }

    if (!invalidProps.isEmpty()) {
      invalidProps.delete(invalidProps.length() - 2, invalidProps.length());
      log.error("message=Invalid preprocessing manifest;jobId={};reason={}",
          manifest.jobId(), invalidProps);
    }

    return invalidProps.isEmpty();
  }
}
