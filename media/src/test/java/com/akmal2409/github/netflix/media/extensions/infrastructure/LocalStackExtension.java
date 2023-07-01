package com.akmal2409.github.netflix.media.extensions.infrastructure;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.utility.DockerImageName;

public class LocalStackExtension implements BeforeAllCallback, AfterAllCallback {


  private LocalStackContainer localStack;

  private Service[] services = {Service.S3};

  public LocalStackExtension() {
  }

  public LocalStackExtension(Service[] services) {
    this.services = services;
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    this.localStack = new LocalStackContainer(
        new DockerImageName("localstack/localstack:2.1")
    ).withServices(services);

    localStack.start();
    System.setProperty("app.media-s3.host", localStack.getEndpoint().toString());
    System.setProperty("aws.accessKeyId", localStack.getAccessKey());
    System.setProperty("aws.secretAccessKey", localStack.getSecretKey());
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) throws Exception {

  }

  public LocalStackContainer getContainer() {
    return localStack;
  }
}
