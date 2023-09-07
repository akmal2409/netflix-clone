package io.github.akmal2409.hls;

/**
 * <a href="https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.3">Specification</a>
 */
public final class MediaPlaylistTags {

  /**
   * #EXT-X-TARGETDURATION:<duration/> tag - floating point duration in seconds (floating since
   * version 3)
   * <a href="https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.3.1">Docs</a>
   */
  public static final String TARGET_DURATION = "EXT-X-TARGETDURATION";
  /**
   * #EXT-X-MEDIA-SEQUENCE:<number/> tag - media sequence number of the first segment in the
   * playlist Default: 0 Required: no
   * <a href="https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.3.2">Docs</a>
   */
  public static final String MEDIA_SEQUENCE = "EXT-X-MEDIA-SEQUENCE";
  /**
   * #EXT-X-DISCONTINUITY-SEQUENCE:<number> Requirements: must appear before first media segment
   * Default: 0 Required: no
   * <a href="https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.3.3">Docs.</a>
   */
  public static final String DISCONTINUITY_SEQUENCE = "EXT-X-DISCONTINUITY-SEQUENCE";
  /**
   * EXT-X-PLAYLIST-TYPE:<value/> Required: no Values: VOD or EVENT
   * <a href="https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.3.5">Docs</a>
   */
  public static final String PLAYLIST_TYPE_TAG = "EXT-X-PLAYLIST-TYPE";
  /**
   * #EXT-X-I-FRAMES-ONLY Required: if each segment starts with an I-Frame
   * <a href="https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.3.6">Docs</a>
   */
  public static final String I_FRAMES_ONLY = "EXT-X-I-FRAMES-ONLY";
  /**
   * #EXTINF:<duration>,[<title>] Required: yes Note: duration is floating point from version 3 Next
   * line is followed by the URI of the resource
   * <a href="https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.2.1">Docs</a>
   */
  public static final String EXTINF = "EXTINF";

  // Media Segment tags come below
  /**
   * EXT-X-ENDLIST Required: if media segment list provided
   * <a href="https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.3.4">Docs</a>
   */
  public static final String END_LIST_TAG = "EXT-X-ENDLIST";

  private MediaPlaylistTags() {
    throw new IllegalStateException("Cannot instantiate class with constants");
  }
}
