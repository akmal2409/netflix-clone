package io.github.akmal2409;

import java.nio.file.Path;
import java.util.UUID;

public class Configuration {

  /**
   * To avoid creation of a simple maven project that will just duplicate some of the code to transcode the audio,
   * this worker will contain code for both and the role of it could be set at the startup
   */
  public static enum WorkerMode {
    VIDEO_TRANSCODER, AUDIO_TRANSCODER
  }

  public String getRabbitMqHost() {
    return "localhost";
  }

  public int getRabbitMqPort() {
    return 5672;
  }

  public String getTaskQueueName() {
    if (WorkerMode.VIDEO_TRANSCODER.equals(getWorkerMode())) {
      return getVideoTaskQueueName();
    } else {
      return getAudioTaskQueueName();
    }
  }

  public String getVideoTaskQueueName() {
    return "video-transcoding-queue";
  }

  public String getAudioTaskQueueName() {
    return "audio-transcoding-queue";
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


  public WorkerMode getWorkerMode() {
    return WorkerMode.AUDIO_TRANSCODER;
  }
}
