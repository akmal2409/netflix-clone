package io.github.akmal2409.hls;

/**
 * A collection of constants that are shared between Media and Master playlist files.
 */
public final class CommonPlaylistTags {

  /**
   * Tag that indicates that the file is Extended M3U. Must come as the very first tag of any
   * playlist file.
   * <a href="https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.1.1">Docs</a>
   */
  public static final String PLAYLIST_HEADER = "EXTM3U";
  /**
   * #EXT-X-VERSION:<code>n</code> indicates version of the playlist. Required: no Default: 1
   * <a href="https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.1.2">Docs</a>
   */
  public static final String VERSION = "EXT-X-VERSION";

  private CommonPlaylistTags() {
    throw new IllegalStateException("Cannot instantiate class with constants");
  }

}
