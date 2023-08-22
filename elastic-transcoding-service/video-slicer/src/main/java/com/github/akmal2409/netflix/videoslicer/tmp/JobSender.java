package com.github.akmal2409.netflix.videoslicer.tmp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.akmal2409.netflix.videoslicer.job.PreprocessingManifest;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class JobSender {

  public static void main(String[] args) {

    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost("localhost");
    ObjectMapper mapper = new ObjectMapper();


    try (Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel()) {

      final var jobId = UUID.randomUUID();

      PreprocessingManifest manifest = new PreprocessingManifest(
          jobId,
          "raw", "video.mkv",
          "processed", String.format("job-%s",jobId), 5
      );

      AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                                  .contentEncoding(StandardCharsets.UTF_8.name())
                                  .contentType("application/json")
                                  .build();

      channel.basicPublish("",
          "video-preprocess-job-queue", props,
          mapper.writeValueAsString(manifest).getBytes(StandardCharsets.UTF_8));


    } catch (IOException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }
}
