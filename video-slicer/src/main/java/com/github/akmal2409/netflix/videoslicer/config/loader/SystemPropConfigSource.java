package com.github.akmal2409.netflix.videoslicer.config.loader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SystemPropConfigSource implements ConfigurationSource {

  @Override
  public Map<String, Object> load(Collection<String> keys) {
    final Properties systemProps = System.getProperties();
    final Map<String, Object> configuration = new HashMap<>();

    for (String key: keys) {
        if (systemProps.contains(key)) {
          configuration.put(key, systemProps.get(key));
        }
    }

    return configuration;
  }

  @Override
  public int priorityOrder() {
    return Integer.MAX_VALUE - 1;
  }
}
