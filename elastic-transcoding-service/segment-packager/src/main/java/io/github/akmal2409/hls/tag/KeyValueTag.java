package io.github.akmal2409.hls.tag;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Simple key value tag that has both key and a value (value can be single or attribute list)
 */
class KeyValueTag implements Tag {

  private final String key;
  private final TagValue value;

  KeyValueTag(String key, TagValue value) {
    this.key = key;
    this.value = value;
  }

  public static KeyValueTag fromKV(@NotNull String key, @NotNull TagValue value) {
    return new KeyValueTag(key, value);
  }

  @Override
  public String serialise() {
    return String.format("#%s:%s",
        key, value);
  }

  @Override
  public Optional<TagValue> value() {
    return Optional.of(this.value);
  }

  @Override
  public Optional<String> key() {
    return Optional.of(this.key);
  }
}
