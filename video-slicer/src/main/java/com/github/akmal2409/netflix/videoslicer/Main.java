package com.github.akmal2409.netflix.videoslicer;

import com.github.akmal2409.netflix.videoslicer.config.WorkerConfiguration;
import com.github.akmal2409.netflix.videoslicer.config.loader.ConfigurationLoader;
import com.github.akmal2409.netflix.videoslicer.config.loader.EnvironmentVariableConfigSource;
import com.github.akmal2409.netflix.videoslicer.config.loader.SystemPropConfigSource;
import com.github.akmal2409.netflix.videoslicer.config.loader.TomlConfigSource;
import java.nio.file.Path;
import java.util.List;


public class Main {

  public static void main(String[] args) {
    final var loader = new ConfigurationLoader<>(List.of(
        new TomlConfigSource(Path.of("./config.toml")),
        new SystemPropConfigSource(),
        new EnvironmentVariableConfigSource()
    ), WorkerConfiguration.class);

    new WorkerApplication(loader)
        .run();


  }
}
