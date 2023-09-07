package io.github.akmal2409.hls.tag;

import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * HLS key only tag that has no value of form #$Key
 */
class KeyTag implements Tag {

  private final String key;

  KeyTag(@NotNull String key) {
    this.key = Objects.requireNonNull(key, "Key was null");
  }

  public KeyTag from(@NotNull String key) {
    return new KeyTag(key);
  }

  @Override
  public String serialise() {
    return String.format("#%s", this.key);
  }

  @Override
  public Optional<TagValue> value() {
    return Optional.empty();
  }

  /**
   * HLS playlist key tag that has only key and no value
   */
  @Override
  public Optional<String> key() {
    return Optional.of(this.key);
  }
}
