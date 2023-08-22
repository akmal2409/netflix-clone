package com.github.akmal2409.netflix.videoslicer.config.loader;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TomlConfigSource implements ConfigurationSource {

  private final Path filePath;

  public TomlConfigSource(Path filePath) {
    this.filePath = filePath;
  }

  @Override
  public Map<String, Object> load(Collection<String> keys) {
    if (!Files.exists(this.filePath)) {
      throw new ConfigurationLoadingException(String.format("Could not load configuration because file %s is missing", filePath));
    }

    try(final var config = FileConfig.of(this.filePath)) {
      config.load();

      final Map<String, Object> inlineKeyConfig = new HashMap<>();
      final Set<String> requiredKeySet = new HashSet<>(keys);

      extractEntries(config, "", inlineKeyConfig, requiredKeySet);

      return inlineKeyConfig;
    }
  }

  /**
   * Inlines the keys from configuration.
   * For example, nested keys produce nested maps, what this function is doing
   * is that it parses the entries and inlines the keys.
   *
   * @param config configuration map
   * @param keyPrefix key prefix so far (e.g key1.key2)
   * @param target where to place values
   * @param requiredKeySet the set of keys that need to be loaded from the TOML file
   */
  private void extractEntries(Config config, String keyPrefix, Map<String, Object> target,
      Set<String> requiredKeySet) {
    String key;

    for (Config.Entry entry: config.entrySet()) {
      if (keyPrefix.isEmpty()) { // at the first level of recursion we don't have to append key separator
        key = entry.getKey();
      } else {
        key = String.format("%s.%s", keyPrefix, entry.getKey());
      }

      if (entry.getValue() instanceof Config subConfig) {
        extractEntries(subConfig, key, target, requiredKeySet);
      } else {
        if (requiredKeySet.contains(key)) {
          target.put(key, entry.getValue());
        }
      }
    }
  }

  @Override
  public int priorityOrder() {
    return 0;
  }
}
