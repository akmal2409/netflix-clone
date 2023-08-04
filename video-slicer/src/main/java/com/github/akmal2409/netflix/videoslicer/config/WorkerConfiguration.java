package com.github.akmal2409.netflix.videoslicer.config;

import com.github.akmal2409.netflix.videoslicer.config.loader.ConfigurationProperty;
import java.nio.file.Path;

public class WorkerConfiguration {

  @ConfigurationProperty(
      key = "rabbitmq.host",
      description = "RabbitMQ host",
      defaultValue = "localhost"
  )
  private final String rabbitmqHost = "localhost";

  @ConfigurationProperty(
      key = "rabbitmq.port",
      description = "RabbitMQ port",
      defaultValue = "5672"
  )
  private final int rabbitmqPort = 5672;

  @ConfigurationProperty(
      key = "worker.concurrent-jobs",
      description = "Number of concurrent jobs worker can execute at once",
      defaultValue = "1"
  )
  private final int workerConcurrentJobs = 1;

  @ConfigurationProperty(
      key = "worker.jobs-file-folder",
      description = "Absolute path a file folder to store job related files"
  )
  private final String jobsFileFolder =
      Path.of(System.getProperty("java.io.tmpdir"), ".preprocess-worker").toString();

  @ConfigurationProperty(
      key = "worker.job-queue-name",
      description = "Job queue name"
  )
  private final String jobQueue = null;

  @ConfigurationProperty(
      key = "binaries.ffmpeg-path",
      description = "Absolute path to ffmpeg binary"
  )
  private final String ffmpegPath = null;
  @ConfigurationProperty(
      key = "binaries.ffprobe-path",
      description = "Absolute path to ffprobe binary"
  )
  private final String ffprobePath = null;


  @ConfigurationProperty(
      key = "aws.s3.endpoint",
      description = "Absolute path to ffprobe binary"
  )
  private final String s3Endpoint = null;

  @ConfigurationProperty(
      key = "aws.s3.region",
      description = "Absolute path to ffprobe binary",
      defaultValue = "us-east-1"
  )
  private final String s3Region = "us-east-1";



  public String getRabbitmqHost() {
    return rabbitmqHost;
  }

  public int getRabbitmqPort() {
    return rabbitmqPort;
  }

  public int getWorkerConcurrentJobs() {
    return workerConcurrentJobs;
  }

  public String getJobsFileFolder() {
    return jobsFileFolder;
  }

  public String getJobQueue() {
    return jobQueue;
  }

  public String getFfmpegPath() {
    return ffmpegPath;
  }

  public String getFfprobePath() {
    return ffprobePath;
  }

  public String getS3Endpoint() {
    return s3Endpoint;
  }

  public String getS3Region() {
    return s3Region;
  }


  @Override
  public String toString() {
    return "WorkerConfiguration{" +
               "rabbitmqHost='" + rabbitmqHost + '\'' +
               ", rabbitmqPort=" + rabbitmqPort +
               ", workerConcurrentJobs=" + workerConcurrentJobs +
               ", ffmpegPath='" + ffmpegPath + '\'' +
               ", ffprobePath='" + ffprobePath + '\'' +
               ", s3Endpoint='" + s3Endpoint + '\'' +
               ", s3Region='" + s3Region + '\'' +
               ", jobsFileFolder='" + jobsFileFolder + '\'' +
               ", jobQueue='" + jobQueue + '\'' +
               '}';
  }
}
