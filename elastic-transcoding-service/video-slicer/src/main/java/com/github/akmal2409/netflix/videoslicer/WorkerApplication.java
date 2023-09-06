package com.github.akmal2409.netflix.videoslicer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.akmal2409.netflix.videoslicer.config.WorkerConfiguration;
import com.github.akmal2409.netflix.videoslicer.config.loader.ConfigurationLoader;
import com.github.akmal2409.netflix.videoslicer.exception.AlreadyRunningException;
import com.github.akmal2409.netflix.videoslicer.exception.AsyncMessagingPlatformException;
import com.github.akmal2409.netflix.videoslicer.exception.DeadWorkerException;
import com.github.akmal2409.netflix.videoslicer.job.JobConsumer;
import com.github.akmal2409.netflix.videoslicer.job.S3Store;
import com.github.akmal2409.netflix.videoslicer.processing.MediaExtractor;
import com.github.akmal2409.netflix.videoslicer.processing.analyser.VideoAnalyser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

/**
 * Main bootstrap application logic that configures and starts up the worker.
 */
public class WorkerApplication {

  private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);
  private final ConfigurationLoader<WorkerConfiguration> configLoader;
  private WorkerConfiguration configuration;

  private boolean running;
  private boolean shutdown;
  private boolean configured;

  private final ReentrantLock lock;

  private DependencyFactory dependencyFactory;

  private MediaExtractor mediaExtractor;
  private VideoAnalyser videoAnalyser;
  private S3Client s3Client;
  private S3TransferManager transferManager;
  private S3Store s3VideoStore;

  private ConnectionFactory rabbitConnectionFactory;

  private ObjectMapper mapper;

  private Connection activeConnection;
  private Channel activeChannel;

  public WorkerApplication(ConfigurationLoader<WorkerConfiguration> configLoader) {
    this.configLoader = configLoader;
    this.running = false;
    this.shutdown = false;
    this.configured = false;
    this.lock = new ReentrantLock();
  }

  // TODO: REFACTOR THIS PSEUDO THREAD SAFETY!!!!!!! (Disgrace to myself)
  public void run() {
    markAsRunningIfNotAlready();
    log.debug("Starting worker application");
    loadConfiguration();

    try {
      lock.lock();
      this.activeConnection = rabbitConnectionFactory.newConnection();
      this.activeChannel = activeConnection.createChannel();

      log.debug("Established AMQP connection to RabbitMQ at {}:{}",
          configuration.getRabbitmqHost(), configuration.getRabbitmqPort());

      activeChannel.queueDeclare(configuration.getJobQueue(), true, false, false, null);
      activeChannel.basicQos(configuration.getWorkerConcurrentJobs());

      activeChannel.basicConsume(configuration.getJobQueue(), new JobConsumer(
          activeChannel, mediaExtractor, mapper, s3VideoStore,
          Executors.newFixedThreadPool(2), videoAnalyser));

    } catch (IOException | TimeoutException e) {
      throw new AsyncMessagingPlatformException("Issue with AMQP connection", e);
    } finally {
      lock.unlock();
    }


    lock.lock();
    try {
      Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
      running = true;
    } finally {
      lock.unlock();
    }
  }

  public void shutdown() {
    lock.lock();

    try {
      if (shutdown || !running) {
        throw new DeadWorkerException("Worker is not running. Cannot shutdown");
      }

      shutdown = true;

      if (activeChannel != null && activeChannel.isOpen()) {
        activeChannel.close();
      }
      if (activeConnection != null && activeConnection.isOpen()) {
        activeConnection.close();
      }
    } catch (IOException | TimeoutException e) {
      log.error("Exception occurred while shutting down", e);
    } finally {
      lock.unlock();
    }
  }

  private void loadConfiguration() {
    lock.lock();
    if (this.configured) {
      throw new AlreadyRunningException(
          "Cannot configure worker because it has been already configured");
    }
    try {
      log.debug("Loading configuration and instantiating dependencies");
      configuration = configLoader.load();
      dependencyFactory = DependencyFactory.withConfiguration(configuration);
      final var awsCredentialsProvider = dependencyFactory.newAwsCredentialsProvider();
      s3Client = dependencyFactory.newS3Client(awsCredentialsProvider);

      mediaExtractor = dependencyFactory.newVideoSlicer(dependencyFactory.ffmpegExecutor());
      videoAnalyser = dependencyFactory.newVideoAnalyser(dependencyFactory.newJaffreeFFprobe());

      transferManager = dependencyFactory.newS3TransferManager(
          dependencyFactory.newS3AsyncClient(awsCredentialsProvider));
      s3VideoStore = dependencyFactory.videoStore(transferManager);
      rabbitConnectionFactory = dependencyFactory.newConnectionFactory();
      mapper = dependencyFactory.newObjectMapper();

      this.configured = true;
      log.debug("Configuration finished successfully");
    } catch (IOException e) {
      log.error("Error occurred during configuration", e);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Checks if the worker application is already running and in case it is, it will throw
   * {@link AlreadyRunningException}.
   *
   * @throws AlreadyRunningException if the worker is already running
   * @throws DeadWorkerException     if the worker has been shutdown
   */
  private void markAsRunningIfNotAlready() {
    lock.lock();

    try {
      if (this.running) {
        throw new AlreadyRunningException("The worker is already running");
      } else if (this.shutdown) {
        throw new DeadWorkerException("The worker has already been shutdown");
      } else {
        this.running = true;
      }
    } finally {
      lock.unlock();
    }
  }
}
