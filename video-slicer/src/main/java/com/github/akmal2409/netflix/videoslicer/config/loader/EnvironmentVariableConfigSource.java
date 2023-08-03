package com.github.akmal2409.netflix.videoslicer.config.loader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EnvironmentVariableConfigSource implements ConfigurationSource {
  @Override
  public Map<String, Object> load(Collection<String> keys) {
    final Map<String, String> envVariables = System.getenv();
    final var properties = new HashMap<String, Object>();

    for (String key: keys) {
      if (envVariables.containsKey(key)) {
        properties.put(key, envVariables.get(key));
      }
    }

    return properties;
  }

  @Override
  public int priorityOrder() {
    return Integer.MAX_VALUE;
  }
}
