package io.github.akmal2409.hls;

import io.github.akmal2409.hls.tag.Tag;
import io.github.akmal2409.hls.tag.TagValue.AttributeListTagValue;
import io.github.akmal2409.utils.StringUtils;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * <a href="https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.4.2">Docs</a>
 */
public class VariantStream {

  public static final String TAG_KEY = "EXT-X-STREAM-INF";
  public static final String BANDWIDTH_ATTRIBUTE = "BANDWIDTH";
  public static final String AVERAGE_BANDWIDTH_ATTRIBUTE = "AVERAGE-BANDWIDTH";
  public static final String CODECS_ATTRIBUTE = "CODECS";
  public static final String RESOLUTION_ATTRIBUTE = "RESOLUTION";

  public static final String FRAME_RATE_ATTRIBUTE = "FRAME-RATE";
  public static final String HDCP_LEVEL_ATTRIBUTE = "HDCP-LEVEL";

  public static final String AUDIO_ATTRIBUTE = "AUDIO";

  public static final String VIDEO_ATTRIBUTE = "VIDEO";

  public static final String SUBTITLES_ATTRIBUTE = "SUBTITLES";
  public static final String CLOSED_CAPTIONS_ATTRIBUTE = "CLOSED-CAPTIONS";

  final String audioGroupId; // audio rendition group
  final String videoGroupId; // video rendition group
  final String closedCaptionsGruopId; // closed captions rendition group
  final String subtitlesGroupId; // subtitles rendition group


  /**
   * Goes under the variant stream tag as a single value without a prefix or suffix Format:
   * #EXT-X-STREAM-INF:$attribute-list $URI
   */
  final URI playlistURI;

  /**
   * In bits per second
   */
  final Integer bandwidth;

  /**
   * bits per second
   */
  final Integer averageBandwidth;

  final HlsCodec codec;

  final Resolution resolution;

  final Double frameRate;

  private VariantStream(Builder builder) {
    this.audioGroupId = builder.audioGroupId;
    this.videoGroupId = builder.videoGroupId;
    this.subtitlesGroupId = builder.subtitlesGroupId;
    this.closedCaptionsGruopId = builder.closedCaptionsGroupId;

    this.bandwidth = builder.bandwidth;
    this.averageBandwidth = builder.averageBandwidth;

    this.playlistURI = builder.playlistUri;

    this.codec = builder.codec;
    this.resolution = builder.resolution;
    this.frameRate = builder.frameRate;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Tag toTag() {

    final var attributes = new LinkedHashMap<String, String>();

    if (bandwidth != null) {
      attributes.put(BANDWIDTH_ATTRIBUTE, bandwidth.toString());
    }

    if (averageBandwidth != null) {
      attributes.put(AVERAGE_BANDWIDTH_ATTRIBUTE, averageBandwidth.toString());
    }

    if (codec != null) {
      attributes.put(CODECS_ATTRIBUTE, StringUtils.toQuotedString(codec.toString()));
    }

    if (resolution != null) {
      attributes.put(RESOLUTION_ATTRIBUTE, resolution.toString());
    }

    if (frameRate != null) {
      attributes.put(FRAME_RATE_ATTRIBUTE, String.valueOf(Math.round(frameRate * 1_000) / 1000d));
    }

    if (audioGroupId != null) {
      attributes.put(AUDIO_ATTRIBUTE, StringUtils.toQuotedString(audioGroupId));
    }

    if (videoGroupId != null) {
      attributes.put(VIDEO_ATTRIBUTE, StringUtils.toQuotedString(videoGroupId));
    }

    if (subtitlesGroupId != null) {
      attributes.put(SUBTITLES_ATTRIBUTE, StringUtils.toQuotedString(subtitlesGroupId));
    }

    if (closedCaptionsGruopId != null) {
      attributes.put(CLOSED_CAPTIONS_ATTRIBUTE, StringUtils.toQuotedString(closedCaptionsGruopId));
    }

    return Tag.fromKV(TAG_KEY, new AttributeListTagValue(attributes));
  }

  /**
   * Returns id (if present) of media renditions with type AUDIO
   */
  public Optional<String> audioRenditionGroupId() {
    return Optional.ofNullable(this.audioGroupId);
  }

  /**
   * Returns id (if present) of media renditions with type VIDEO
   */
  public Optional<String> videoRenditionGroupId() {
    return Optional.ofNullable(this.videoGroupId);
  }

  /**
   * Returns id (if present) of media renditions with type SUBTITLES
   */
  public Optional<String> subtitlesRenditionGroupId() {
    return Optional.ofNullable(this.subtitlesGroupId);
  }

  /**
   * Returns id (if present) of media renditions with type CLOSED-CAPTIONS
   */
  public Optional<String> closedCaptionsRenditionGroupId() {
    return Optional.ofNullable(this.closedCaptionsGruopId);
  }

  /**
   * Returns playlist URI that contains either audio, video, subtitles or closed captions
   */
  public Optional<URI> playlistURI() {
    return Optional.ofNullable(this.playlistURI);
  }

  /**
   * Returns peak bandwidth of the media segments in bits per second
   */
  public Optional<Integer> bandwidth() {
    return Optional.ofNullable(this.bandwidth);
  }

  /**
   * Returns average bandwidth in bits per second
   */
  public Optional<Integer> averageBandwidth() {
    return Optional.ofNullable(this.averageBandwidth);
  }

  /**
   * Indicates whether the variant stream points to the audio group
   */
  public boolean hasAudio() {
    return !StringUtils.isEmpty(this.audioGroupId);
  }

  /**
   * Indicates whether the variant stream points to the video group
   */
  public boolean hasVideo() {
    return !StringUtils.isEmpty(this.videoGroupId);
  }

  /**
   * Indicates whether the variant stream points to the subtitles group
   */
  public boolean hasSubtitles() {
    return !StringUtils.isEmpty(this.subtitlesGroupId);
  }

  /**
   * Indicates whether the variant stream points to the closed captions group
   */
  public boolean hasClosedCaptions() {
    return !StringUtils.isEmpty(this.closedCaptionsGruopId);
  }

  public static class Builder {


    private String audioGroupId;
    private String videoGroupId;
    private String subtitlesGroupId;
    private String closedCaptionsGroupId;

    private URI playlistUri;

    private Integer bandwidth;
    private Integer averageBandwidth;

    private HlsCodec codec;

    private Resolution resolution;

    private Double frameRate;


    /**
     * Group ID of a rendition group of medias present in the renditions list.
     *
     * @param groupId of the media group
     */
    public Builder audioRenditionGroup(@NotNull String groupId) {
      this.audioGroupId = groupId;
      return this;
    }


    /**
     * Group ID of a rendition group of medias present in the renditions list.
     *
     * @param groupId of the media group
     */
    public Builder videoRenditionGroup(@NotNull String groupId) {
      this.videoGroupId = groupId;
      return this;
    }


    /**
     * Group ID of a rendition group of medias present in the renditions list.
     *
     * @param groupId of the media group
     */
    public Builder subtitlesRenditionGroup(@NotNull String groupId) {
      this.subtitlesGroupId = groupId;
      return this;
    }


    /**
     * Group ID of a rendition group of medias present in the renditions list.
     *
     * @param groupId of the media group
     */
    public Builder closedCaptionsRenditionGroup(@NotNull String groupId) {
      this.closedCaptionsGroupId = groupId;
      return this;
    }

    public Builder playlistUri(URI uri) {
      this.playlistUri = uri;
      return this;
    }

    /**
     * Bandwidth is the value in bits per second signifying the max bandwidth of a segment
     *
     * @param bandwidth bits per second
     */
    public Builder bandwidth(int bandwidth) {
      this.bandwidth = bandwidth;
      return this;
    }

    /**
     * Average bandwidth is the value in bits per second signifying the average bandwidth of a
     * segment
     *
     * @param averageBandwidth bits per second
     */
    public Builder averageBandwidth(int averageBandwidth) {
      this.averageBandwidth = averageBandwidth;
      return this;
    }

    public Builder codec(HlsCodec codec) {
      this.codec = codec;
      return this;
    }

    public Builder resolution(Resolution resolution) {
      this.resolution = resolution;
      return this;
    }

    /**
     * Average frame rate of a segment
     *
     * @param frameRate double value
     */
    public Builder frameRate(double frameRate) {
      this.frameRate = frameRate;
      return this;
    }

    public VariantStream build() {
      return new VariantStream(this);
    }
  }
}
