package io.github.akmal2409;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.rabbitmq.client.ConnectionFactory;
import io.github.akmal2409.job.S3Store;
import io.github.akmal2409.job.Transcoder;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

/**
 * Holds all required dependencies as well as configures them at the start.
 */
public class DependencyContainer {
  private static final Logger log = LoggerFactory.getLogger(DependencyContainer.class);

  private final Configuration configuration;

  private final AtomicReference<ConnectionFactory> rabbitConnectionFactory = new AtomicReference<>();
  private final AtomicReference<ObjectMapper> objectMapper = new AtomicReference<>();
  private final AtomicReference<FFprobe> jaffreeFfprobe = new AtomicReference<>();
  private final AtomicReference<FFmpegExecutor> ffmpegExecutor = new AtomicReference<>();

  private final AtomicReference<S3Store> s3Store = new AtomicReference<>();
  private final AtomicReference<Transcoder> transcoder = new AtomicReference<>();

  private final AtomicReference<S3AsyncClient> s3AsyncClient = new AtomicReference<>();
  private final AtomicReference<S3TransferManager> s3TransferManager = new AtomicReference<>();

  private DependencyContainer(Configuration configuration) {
    this.configuration = configuration;
  }

  public static DependencyContainer from(Configuration configuration) {
    final var container = new DependencyContainer(configuration);

    container.init();
    return container;
  }


  private void init() {
    log.debug("Initialising dependencies");
    this.rabbitConnectionFactory.set(newRabbitMqConnectionFactory());
    this.objectMapper.set(newObjectMapper());
    try {
      this.ffmpegExecutor.set(new FFmpegExecutor(new FFmpeg(configuration.getFfmpegPath()), new net.bramp.ffmpeg.FFprobe(configuration.getFfprobePath())));
    } catch (IOException e) {
      // ignored
    }

    this.jaffreeFfprobe.set(new FFprobe(Path.of(configuration.getBinariesPath())));

    final var awsCredentialsProvider = newAwsCredentialsProvider();

    this.s3AsyncClient.set(newS3AsyncClient(awsCredentialsProvider));
    this.s3TransferManager.set(newS3TransferManager(s3AsyncClient.get()));

    this.s3Store.set(new S3Store(s3TransferManager.get(), Path.of(configuration.getWorkingDirectory())));
    this.transcoder.set(newTranscoder(ffmpegExecutor.get()));
  }

  public Transcoder newTranscoder(FFmpegExecutor executor) {
    return new Transcoder(executor);
  }
  public ObjectMapper newObjectMapper() {
    final var mapper = new ObjectMapper();
    mapper.findAndRegisterModules();

    return mapper;
  }

  public ConnectionFactory newRabbitMqConnectionFactory() {
    final var factory = new ConnectionFactory();
    factory.setHost(configuration.getRabbitMqHost());
    factory.setPort(configuration.getRabbitMqPort());
    return factory;
  }

  public S3AsyncClient newS3AsyncClient(
      AwsCredentialsProvider credentialsProvider) {
    return S3AsyncClient.builder()
               .region(Region.of(configuration.getS3Region()))
               .endpointOverride(URI.create(configuration.getS3Endpoint()))
               .credentialsProvider(credentialsProvider)
               .forcePathStyle(true)
               .build();
  }

  public AwsCredentialsProvider newAwsCredentialsProvider() {
    return DefaultCredentialsProvider.create();
  }

  public S3TransferManager newS3TransferManager(S3AsyncClient asyncClient) {
    return S3TransferManager.builder()
               .s3Client(asyncClient)
               .build();
  }

  public ConnectionFactory getRabbitConnectionFactory() {
    return rabbitConnectionFactory.get();
  }
  public ObjectMapper getObjectMapper() {
    return objectMapper.get();
  }

  public FFmpegExecutor getFfmpegExecutor() {
    return this.ffmpegExecutor.get();
  }

  public FFprobe getJaffreeFfprboe() {
    return this.jaffreeFfprobe.get();
  }

  public S3Store getS3Store() {
    return this.s3Store.get();
  }

  public Transcoder getTranscoder() {
    return this.transcoder.get();
  }
}
