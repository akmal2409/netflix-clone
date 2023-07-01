package com.akmal2409.github.netflix.media.extensions.infrastructure;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresExtension implements BeforeAllCallback, AfterAllCallback {

  private PostgreSQLContainer<?> postgres;

  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"));

    postgres.start();
    System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
    System.setProperty("spring.datasource.username", postgres.getUsername());
    System.setProperty("spring.datasource.password", postgres.getPassword());
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) throws Exception {
    // testcontainers will tear it down
  }
}
