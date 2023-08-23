package io.github.akmal2409;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.akmal2409.job.JobConsumer;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {

    final var configuration = new Configuration();
    log.debug("Starting transcoding worker with ID {}", configuration.getWorkerId());

    final var dependencyContainer = DependencyContainer.from(configuration);
    final ConnectionFactory connectionFactory = dependencyContainer.getRabbitConnectionFactory();

    try {
      final Connection connection = connectionFactory.newConnection();
      final Channel channel = connection.createChannel();

      // set the number of messages to consume without acknowledging
      channel.basicQos(configuration.getConcurrency());

      channel.queueDeclare(configuration.getTaskQueueName(), true, false, false, null);

      channel.basicConsume(configuration.getTaskQueueName(),
          new JobConsumer(channel, dependencyContainer.getObjectMapper(), dependencyContainer.getS3Store(),
              dependencyContainer.getTranscoder()));

      log.debug("Started worker consumer, ready to process jobs");
    } catch (IOException | TimeoutException e) {
      log.error("Exception occurred when establishing AMQP connection", e);
    }
  }
}
