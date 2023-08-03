package com.github.akmal2409.netflix.videoslicer.config.loader;

import java.util.Collection;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Common contract for configuration sources that defines load priority.
 */
interface ConfigurationSource extends Comparable<ConfigurationSource> {
  /**
   * Loads and reads configuration properties from a defined source
   */
  Map<String, Object> load(Collection<String> keys);

  /**
   * Returns priority order of the configuration.
   * The highest priority configuration will be applied at the end meaning that it will override other properties
   * shall collisions happen
   * Note: has an undefined behaviour if priority orders clash
   */
  int priorityOrder();

  @Override
  default int compareTo(@NotNull ConfigurationSource other) {
    return Integer.compare(priorityOrder(), other.priorityOrder());
  }
}
