package io.github.akmal2409;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.akmal2409.job.SegmentConstants;
import io.github.akmal2409.job.TranscodingJobManifest;
import io.github.akmal2409.job.TranscodingJobManifest.VideoQuality;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class JobSender {

  public static void main(String[] args) {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost("localhost");

    ObjectMapper mapper = new ObjectMapper();

    try (final Connection conn = connectionFactory.newConnection();
     final Channel channel = conn.createChannel()) {
      final var jobId = UUID.randomUUID();
      final var job = new TranscodingJobManifest(
          jobId,
          "processed", "job-376c3b41-cdcf-45b5-8773-23e91633c853",
          "transcoded", String.format("job-%s",jobId), 0, 5,
          new String[]{SegmentConstants.segmentIndexToFileName(1), SegmentConstants.segmentIndexToFileName(2), SegmentConstants.segmentIndexToFileName(3)},
          10, 120, new VideoQuality[0], new VideoQuality(1280, 720, 7366)
      );

      channel.basicPublish("", "transcoding-queue", null, mapper.writeValueAsBytes(job));
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (TimeoutException e) {
      throw new RuntimeException(e);
    }
  }
}
