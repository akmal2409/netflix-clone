package com.github.akmal2409.netflix.videoslicer.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.akmal2409.netflix.videoslicer.processing.VideoSlicer;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobConsumer extends DefaultConsumer {

  private static final Logger log = LoggerFactory.getLogger(JobConsumer.class);

  private final VideoSlicer videoSlicer;
  private final ObjectMapper objectMapper;
  private final S3VideoStore videoStore;
  private final Executor executor;

  public JobConsumer(Channel channel, VideoSlicer videoSlicer, ObjectMapper objectMapper,
      S3VideoStore videoStore, Executor executor) {
    super(channel);
    this.videoSlicer = videoSlicer;
    this.objectMapper = objectMapper;
    this.videoStore = videoStore;
    this.executor = executor;
  }

  /**
   * The processing pipeline is simple. Fistly, when the message is received, we try to construct
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
      final JobVideoSource videoSource = videoStore.downloadSource(manifest.jobId(),
          manifest.sourceBucket(), manifest.sourceFileKey());

      final var audioFilePath = videoSource.filePath().getParent().resolve("audio.m4a");

      final var audioExtractionFuture = CompletableFuture.runAsync(() ->
                                                                       videoSlicer.extractAudio(
                                                                           videoSource.filePath(),
                                                                           audioFilePath),
          executor);

      // while the audio task is running async, we can wait for segmentation to finish and then index all files
      videoSlicer.slice(
          videoSource.filePath(),
          videoSource.filePath().getParent().resolve("segments"),
          Duration.ofMillis(manifest.segmentDurationMs()),
          "segment-%03d.mp4");

      audioExtractionFuture.join();
    }
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

    if (manifest.segmentDurationMs() < 1000) {
      invalidProps.append("segmentDurationMs is less than 1000, ");
    }

    if (!invalidProps.isEmpty()) {
      invalidProps.delete(invalidProps.length() - 2, invalidProps.length());
      log.error("message=Invalid preprocessing manifest;jobId={};reason={}",
          manifest.jobId(), invalidProps);
    }

    return invalidProps.isEmpty();
  }
}
