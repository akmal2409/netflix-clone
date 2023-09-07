package io.github.akmal2409.hls;

/**
 * <a href="https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.4">Docs</a>
 */
public final class MasterPlaylistTags {

  /**
   * #EXT-X-MEDIA represents multiple renditions of the same presentation Example: different audio
   * language tracks Required: no
   * <a href="https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.4.1">Docs</a>
   */
  public static final String EXT_X_MEDIA = "EXT-X-MEDIA";

  private MasterPlaylistTags() {
    throw new IllegalStateException("Cannot instantiate class with constants");
  }

  public static final class MediaAttributeListKeys {

    /**
     * Valid values: AUDIO, VIDEO, SUBTITLES, CLOSED-CAPTIONS Required: yes
     */
    public static final String TYPE = "TYPE";
    /**
     * If TYPE is CLOSED-CAPTIONS URI must not be present Required: no Type: Enumerated string
     */
    public static final String URI = "URI";
    /**
     * Group of the rendition Required: yes Type: Quoted string
     */
    public static final String GROUP_ID = "GROUP-ID";
    /**
     * Values: <a href="https://datatracker.ietf.org/doc/html/rfc5646">Docs</a> Required: no Type:
     * Quoted string
     */
    public static final String LANGUAGE = "LANGUAGE";
    /**
     * Values: <a href="https://datatracker.ietf.org/doc/html/rfc5646">Docs</a> Required: no Type:
     * Quoted string
     */
    public static final String ASSOC_LANGUAGE = "ASSOC-LANGUAGE";
    /**
     * Human readable description of the rendition Required: yes Type: Quoted string
     */
    public static final String NAME = "NAME";
    /**
     * Whether to play this rendition if there is no input from the user Default: NO Values: YES, NO
     * Required: no Type: Enumerated String
     */
    public static final String DEFAULT = "DEFAULT";
    /**
     * Values: YES, NO Required no Type: Enumerated String
     */
    public static final String AUTOSELECT = "AUTOSELECT";
    /**
     * If yes then it indicates that the content is essential Only when the type is SUBTITLES
     * Required: no Values: YES, NO Type: Enumerated String
     */
    public static final String FORCED = "FORCED";
    /**
     * Required: if type is CC Only when type is CLOSED-CAPTIONS Type: Quoted string
     */
    public static final String INTSTREAM_ID = "INTSTREAM-ID";

    private MediaAttributeListKeys() {
      throw new IllegalStateException("Cannot instantiate class with constants");
    }
  }

}
