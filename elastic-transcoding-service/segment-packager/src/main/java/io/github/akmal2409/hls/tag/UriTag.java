package io.github.akmal2409.hls.tag;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * HLS URI tag that only contains URI without # prefix and value
 */
class UriTag implements Tag {

  private final URI uri;

  UriTag(URI uri) {
    this.uri = Objects.requireNonNull(uri, "URI was null");
  }

  /**
   * Returns instance of a URI tag that has only URI as value
   *
   * @param uri can be absolute or relative
   */
  public static UriTag from(@NotNull URI uri) {
    return new UriTag(uri);
  }

  /**
   * No value in this case, only tag key
   */
  @Override
  public Optional<TagValue> value() {
    return Optional.empty();
  }

  /**
   * URI Tag does not have a key
   */
  @Override
  public Optional<String> key() {
    return Optional.empty();
  }

  /**
   * Since it's a URI tag it has only value without any key or prefix. URI can be absolute or
   * relative
   */
  @Override
  public String serialise() {
    return this.uri.toString();
  }

  @Override
  public String toString() {
    return serialise();
  }
}
