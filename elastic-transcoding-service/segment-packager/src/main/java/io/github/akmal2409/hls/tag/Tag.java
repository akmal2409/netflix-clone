package io.github.akmal2409.hls.tag;

import java.net.URI;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Common interface definition for HLS playlist tags
 */
public interface Tag {

  /**
   * Constructs an instance of a URI HLS tag
   *
   * @param uri either absolute or relative
   */
  static Tag fromUri(@NotNull URI uri) {
    return new UriTag(uri);
  }

  /**
   * Constructs key only HLS tag
   *
   * @param key tag key
   */
  static Tag fromKey(@NotNull String key) {
    return new KeyTag(key);
  }

  /**
   * Returns default key value HLS playlist tag
   *
   * @param key      of the tag
   * @param tagValue either single or attribute list
   */
  static Tag fromKV(@NotNull String key, @NotNull TagValue tagValue) {
    return new KeyValueTag(key, tagValue);
  }

  /**
   * Returns serialised version of the tag in format #$TAG(:$value(s))? Or in case of a URI tag, the
   * prefix of # is omitted
   */
  String serialise();

  /**
   * Returns tag value which can be no value, single value or attribute list
   */
  Optional<TagValue> value();

  /**
   * Returns key, which may not be present in case of a URI value only
   */
  Optional<String> key();
}
