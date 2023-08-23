package io.github.akmal2409;

import java.nio.file.Path;
import java.util.UUID;

public class Configuration {

  public String getRabbitMqHost() {
    return "localhost";
  }

  public int getRabbitMqPort() {
    return 5672;
  }

  public String getTaskQueueName() {
    return "transcoding-queue";
  }

  public int getConcurrency() {
    return 1;
  }

  public String getWorkerId() {
    return UUID.randomUUID().toString();
  }

  public String getBinariesPath() {
    return "/opt/homebrew/bin";
  }

  public String getFfmpegPath() {
    return "/opt/homebrew/bin/ffmpeg";
  }

  public String getFfprobePath() {
    return "/opt/homebrew/bin";
  }

  public String getS3Region() {
    return "us-east-1";
  }

  public String getS3Endpoint() {
    return "http://localhost:9000";
  }

  public String getWorkingDirectory() {
    return Path.of(System.getProperty("java.io.tmpdir"), ".transcoder-worker").toString();
  }
}
