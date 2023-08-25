package io.github.akmal2409.job.audio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.github.akmal2409.job.InvalidManifestException;
import io.github.akmal2409.job.S3Store;
import io.github.akmal2409.job.Transcoder;
import io.github.akmal2409.utils.FileUtils;
import io.github.akmal2409.utils.StringUtils;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of an single audio file transcoder.
 * The way it works is that it receives the transcoding job manifest from the queue, downloads the audio from S3 bucket,
 * transcodes it and uploads back
 */
public class AudioTranscodingJobConsumer extends DefaultConsumer {

  private static final Logger log = LoggerFactory.getLogger(AudioTranscodingJobConsumer.class);

  private final S3Store s3Store;
  private final ObjectMapper mapper;
  private final Transcoder transcoder;

  public AudioTranscodingJobConsumer(Channel channel, S3Store s3Store, ObjectMapper mapper,
      Transcoder transcoder) {
    super(channel);
    this.s3Store = s3Store;
    this.mapper = mapper;
    this.transcoder = transcoder;
  }


  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
      byte[] body) throws IOException {

      AudioTranscodingJobManifest manifest = null;
    try {
      manifest = mapper.readValue(body, AudioTranscodingJobManifest.class);

      validateManifest(manifest);
      log.debug(
          "message=Validated manifest. Starting with the job;jobId={};consumerTag={};jobType=audio_transcoding_job",
          manifest.jobId(), consumerTag);
    } catch (InvalidManifestException | JsonProcessingException e) {
      // TODO: redirect to some DLQ for alerts and monitoring
      getChannel().basicAck(envelope.getDeliveryTag(), false);
      throw e;
    }

    final Path audioFilePath = s3Store.downloadSamePrefixFiles(manifest.sourceBucket(),
        manifest.sourceKeyPrefix(), manifest.jobId().toString(), manifest.fileName()).resolve(manifest.fileName());

    log.debug(
        "message=Downloaded audio file;bucket={};key={};jobId={};consumerTag={};jobType=audio_transcoding_job",
        manifest.sourceBucket(), manifest.sourceKeyPrefix().concat(manifest.fileName()),
        manifest.jobId(), consumerTag);

    try {
      final var processedAudio = audioFilePath.getParent().resolve("processed").resolve(manifest.fileName());

      transcoder.transcodeAudio(audioFilePath, processedAudio, manifest.codec(), manifest.targetBitRate());
      log.debug(
          "message=Transcoded audio;jobId={};consumerTag={};jobType=audio_transcoding_job",
          manifest.jobId(), consumerTag);

      s3Store.uploadProcessedFiles(manifest.outputBucket(), manifest.outputKeyPrefix(),
          processedAudio.getParent());
      log.debug(
          "message=Uploaded transcoded audio file;bucket={};key={};jobId={};consumerTag={};jobType=audio_transcoding_job",
          manifest.outputBucket(), manifest.outputKeyPrefix().concat(manifest.fileName()),
          manifest.jobId(), consumerTag);

    } finally {
      FileUtils.deleteDirectory(audioFilePath.getParent());
    }
  }

  private void validateManifest(AudioTranscodingJobManifest manifest) {

    if (manifest.jobId() == null) {
      throw new InvalidManifestException("jobId", manifest.sourceKeyPrefix(), "empty");
    }

    if (StringUtils.isEmpty(manifest.sourceBucket())) {
      throw new InvalidManifestException("sourceBucket", manifest.sourceKeyPrefix(), "empty");
    }

    if (StringUtils.isEmpty(manifest.sourceKeyPrefix())) {
      throw new InvalidManifestException("sourceKeyPrefix", manifest.sourceKeyPrefix(), "empty");
    }

    if (StringUtils.isEmpty(manifest.fileName())) {
      throw new InvalidManifestException("filename", manifest.fileName(), "empty");
    }

    if (manifest.targetBitRate() < 100) {
      throw new InvalidManifestException("targetBitRate", manifest.sourceKeyPrefix(), "smaller than 100K");
    }

    if (StringUtils.isEmpty(manifest.codec())) {
      throw new InvalidManifestException("codec", manifest.codec(), "empty");
    }

    if (StringUtils.isEmpty(manifest.outputKeyPrefix())) {
      throw new InvalidManifestException("outputKeyPrefix", manifest.outputKeyPrefix(), "empty");
    }

    if (StringUtils.isEmpty(manifest.outputBucket())) {
      throw new InvalidManifestException("outputBucket", manifest.outputBucket(), "empty");
    }
  }
}
