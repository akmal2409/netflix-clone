package io.github.akmal2409.hls;

/**
 * Common interface for Media and Master HLS playlists
 */
public interface Playlist {

  /**
   * Writes the whole HLS playlist (either media or master) as a string
   */
  String serialise();

  Type type();

  enum Type {
    MASTER, MEDIA
  }
}
