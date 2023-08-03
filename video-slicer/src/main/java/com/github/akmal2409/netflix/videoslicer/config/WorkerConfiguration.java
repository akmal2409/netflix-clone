package com.github.akmal2409.netflix.videoslicer.config;

import com.github.akmal2409.netflix.videoslicer.config.loader.ConfigurationProperty;

public class WorkerConfiguration {

  @ConfigurationProperty(
      key = "rabbitmq.host",
      description = "RabbitMQ host",
      defaultValue = "localhost"
  )
  private String rabbitmqHost = "localhost";

  @ConfigurationProperty(
      key = "rabbitmq.port",
      description = "RabbitMQ port",
      defaultValue = "5672"
  )
  private int rabbitmqPort = 5672;

  @ConfigurationProperty(
      key = "worker.concurrent-jobs",
      description = "Number of concurrent jobs worker can execute at once",
      defaultValue = "1"
  )
  private int workerConcurrentJobs = 1;

  @ConfigurationProperty(
      key = "binaries.ffmpeg-path",
      description = "Absolute path to ffmpeg binary"
  )
  private String ffmpegPath;
  @ConfigurationProperty(
      key = "binaries.ffprobe-path",
      description = "Absolute path to ffprobe binary"
  )
  private String ffprobePath;

  @Override
  public String toString() {
    return "WorkerConfiguration{" +
               "rabbitmqHost='" + rabbitmqHost + '\'' +
               ", rabbitmqPort=" + rabbitmqPort +
               ", workerConcurrentJobs=" + workerConcurrentJobs +
               ", ffmpegPath='" + ffmpegPath + '\'' +
               ", ffprobePath='" + ffprobePath + '\'' +
               '}';
  }

  public String getRabbitmqHost() {
    return rabbitmqHost;
  }

  public int getRabbitmqPort() {
    return rabbitmqPort;
  }

  public int getWorkerConcurrentJobs() {
    return workerConcurrentJobs;
  }

  public String getFfmpegPath() {
    return ffmpegPath;
  }

  public String getFfprobePath() {
    return ffprobePath;
  }
}
