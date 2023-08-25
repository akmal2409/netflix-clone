package io.github.akmal2409;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.akmal2409.Configuration.WorkerMode;
import io.github.akmal2409.job.audio.AudioTranscodingJobManifest;
import io.github.akmal2409.job.video.SegmentConstants;
import io.github.akmal2409.job.video.TranscodingJobManifest;
import io.github.akmal2409.job.video.TranscodingJobManifest.VideoQuality;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class JobSender {

  private static final WorkerMode MODE = WorkerMode.AUDIO_TRANSCODER;

  public static void main(String[] args) {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost("localhost");

    ObjectMapper mapper = new ObjectMapper();

    try (final Connection conn = connectionFactory.newConnection();
     final Channel channel = conn.createChannel()) {
      final var jobId = UUID.fromString("376c3b41-cdcf-45b5-8773-23e91633c853");
//      final var jobId = UUID.randomUUID();
      final var job = getJob(jobId);

      channel.basicPublish("", getQueue(), null, mapper.writeValueAsBytes(job));
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getQueue() {
    if (WorkerMode.AUDIO_TRANSCODER.equals(MODE)) {
      return "audio-transcoding-queue";
    } else {
      return "video-transcoding-queue";
    }
  }


  public static Object getJob(UUID jobId) {
    if (WorkerMode.AUDIO_TRANSCODER.equals(MODE)) {
      return new AudioTranscodingJobManifest(
          jobId,
          "processed", "job-376c3b41-cdcf-45b5-8773-23e91633c853",
          "audio.m4a", 320_000, "aac",
          "transcoded", String.format("job-%s",jobId)
      );
    } else {
      return new TranscodingJobManifest(
          jobId,
          "processed", "job-376c3b41-cdcf-45b5-8773-23e91633c853",
          "transcoded", String.format("job-%s",jobId), 3, 5,
          new String[]{SegmentConstants.segmentIndexToFileName(6)},
          36, 120, new VideoQuality[]{
          new VideoQuality(1280, 720, 6000),
          new VideoQuality(1280, 720, 5000),
          new VideoQuality(1280, 720, 4000),

          new VideoQuality(640, 360, 3000),

      }, new VideoQuality(1280, 720, 7366)
      );
    }
  }
}
