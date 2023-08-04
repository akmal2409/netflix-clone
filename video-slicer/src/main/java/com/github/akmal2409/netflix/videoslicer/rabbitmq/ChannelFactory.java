package com.github.akmal2409.netflix.videoslicer.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.jetbrains.annotations.NotNull;

/**
 * A factory class responsible for creating/closing channels and connections
 * to RabbitMQ host
 */
public class ChannelFactory implements AutoCloseable{
  private final ConnectionFactory connectionFactory;
  private final Connection connection;


  public ChannelFactory(String host, int port) throws IOException, TimeoutException {
    this.connectionFactory = configureConnectionFactory(host, port);
    this.connection = this.connectionFactory.newConnection();
  }

  @Override
  public void close() throws Exception {
    if (this.connection.isOpen()) {
      this.connection.close();
    }
  }

  public Channel newChannel() throws IOException {
    return this.connection.createChannel();
  }

  private ConnectionFactory configureConnectionFactory(@NotNull String host, int port) {
    final var factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setPort(port);

    return factory;
  }
}
