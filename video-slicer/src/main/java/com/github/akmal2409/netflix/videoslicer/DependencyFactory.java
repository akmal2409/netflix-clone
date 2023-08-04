package com.github.akmal2409.netflix.videoslicer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.akmal2409.netflix.videoslicer.config.WorkerConfiguration;
import com.github.akmal2409.netflix.videoslicer.job.S3VideoStore;
import com.github.akmal2409.netflix.videoslicer.processing.FFmpegVideoSlicer;
import com.github.akmal2409.netflix.videoslicer.processing.VideoSlicer;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

/**
 * Configures the dependencies based on the {@link WorkerConfiguration}
 */
public class DependencyFactory {
  private final WorkerConfiguration configuration;

  private DependencyFactory(WorkerConfiguration configuration) {
    this.configuration = configuration;
  }

  public static DependencyFactory withConfiguration(WorkerConfiguration configuration) {
    return new DependencyFactory(configuration);
  }

  public AwsCredentialsProvider newAwsCredentialsProvider() {
    return DefaultCredentialsProvider.create(); // checks system and env variables
  }

  public S3Client newS3Client(AwsCredentialsProvider credentialsProvider) {
    return S3Client.builder()
               .region(Region.of(configuration.getS3Region()))
               .endpointOverride(URI.create(configuration.getS3Endpoint()))
               .credentialsProvider(credentialsProvider)
               .forcePathStyle(true)
               .build();
  }

  public S3AsyncClient newS3AsyncClient(AwsCredentialsProvider credentialsProvider) {
    return S3AsyncClient.builder()
               .region(Region.of(configuration.getS3Region()))
               .endpointOverride(URI.create(configuration.getS3Endpoint()))
               .credentialsProvider(credentialsProvider)
               .forcePathStyle(true)
               .build();
  }

  public S3TransferManager newS3TransferManager(S3AsyncClient asyncClient) {
    return S3TransferManager.builder()
               .s3Client(asyncClient)
               .build();
  }

  public FFmpegExecutor ffmpegExecutor() throws IOException {
    return new FFmpegExecutor(
        new FFmpeg(configuration.getFfmpegPath()),
        new FFprobe(configuration.getFfprobePath())
    );
  }

  public ConnectionFactory newConnectionFactory() {
    final var factory = new ConnectionFactory();
    factory.setHost(configuration.getRabbitmqHost());
    factory.setPort(configuration.getRabbitmqPort());

    return factory;
  }

  public ObjectMapper newObjectMapper() {
    return new ObjectMapper()
               .findAndRegisterModules();
  }

  public VideoSlicer newVideoSlicer(FFmpegExecutor executor) {
    return FFmpegVideoSlicer.withExecutor(executor);
  }

  public S3VideoStore videoStore(S3TransferManager transferManager) {
    return new S3VideoStore(Path.of(configuration.getJobsFileFolder()),
        transferManager);
  }
}
