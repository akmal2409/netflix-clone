package io.github.akmal2409.hls;

import io.github.akmal2409.hls.tag.Tag;
import io.github.akmal2409.hls.tag.TagValue.AttributeListTagValue;
import io.github.akmal2409.utils.StringUtils;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a single media rendition in the HLS master playlist. Multiple renditions with the same
 * groupId make up a rendition group.
 */
public class MediaRendition {

  static final String TAG_KEY = "EXT-X-MEDIA";
  static final String TYPE_ATTRIBUTE = "TYPE";
  static final String URI_ATTRIBUTE = "URI";
  static final String GROUP_ID_ATTRIBUTE = "GROUP-ID";
  static final String LANGUAGE_ATTRIBUTE = "LANGUAGE";
  static final String ASSOC_LANGUAGE_ATTRIBUTE = "ASSOC_LANGUAGE";
  static final String NAME_ATTRIBUTE = "NAME";
  static final String DEFAULT_ATTRIBUTE = "DEFAULT";
  static final String AUTOSELECT_ATTRIBUTE = "AUTOSELECT";
  static final String FORCED_ATTRIBUTE = "FORCED";
  static final String INSTREAM_ID_ATTRIBUTE = "INSTREAM-ID";

  /**
   * Group ID of the media definition. Multiple medias with same id make up a rendition group
   * Required: yes
   */
  final String groupId;

  /**
   * Name of the rendition group Required: yes
   */
  final String name;

  /**
   * Media type Required: yes
   */
  final MediaRendition.Type type; // required attribute
  /**
   * URI to the media playlist Required: no
   */
  final URI uri;

  /**
   * Language of the rendition
   * <a href="https://datatracker.ietf.org/doc/html/rfc5646">Values</a>
   * Required: no
   */
  final String language;

  /**
   * E.g. written vs spoken dialect Required: no
   */
  final String associatedLanguage;

  /**
   * Whether the current rendition is default Required: no Default: NO
   */
  final Confirmation defaultRendition;

  /**
   * Whether to autoselect the rendition group Required: no Default: NO
   */
  final Confirmation autoSelect;

  /**
   * Only for closed captions Required: yes if subtitles is the type
   */
  final Confirmation forced;

  /**
   * Rendition within the segments in the Media Paylist Required: if closed captions
   */
  final String inStreamId;

  private MediaRendition(Builder builder) {
    this.groupId = builder.groupId;
    this.name = builder.name;
    this.type = builder.type;

    this.uri = builder.uri;
    this.language = builder.language;
    this.associatedLanguage = builder.associatedLanguage;

    this.defaultRendition = builder.defaultRendition;
    this.autoSelect = builder.autoSelect;

    if (!Type.SUBTITLES.equals(this.type) && builder.forced != null) {
      throw new IllegalArgumentException(
          "Cannot set forced attribute because type is not subtitles");
    } else {
      this.forced = builder.forced;
    }

    if (!Type.CLOSED_CAPTIONS.equals(this.type) && builder.inStreamId != null) {
      throw new IllegalArgumentException(
          "Cannot set INSTREAM-ID if the type is not CLOSED-CAPTIONS");
    } else {
      this.inStreamId = builder.inStreamId;
    }
  }

  public static Builder builder(@NotNull String groupId, @NotNull String name,
      @NotNull Type type) {
    return new Builder(groupId, name, type);
  }

  public Tag toTag() {

    final var attributes = new LinkedHashMap<String, String>();

    attributes.put(TYPE_ATTRIBUTE, type.value);

    attributes.put(GROUP_ID_ATTRIBUTE, StringUtils.toQuotedString(groupId));
    attributes.put(NAME_ATTRIBUTE, StringUtils.toQuotedString(name));

    if (uri != null) {
      attributes.put(URI_ATTRIBUTE, StringUtils.toQuotedString(uri.toString()));
    }

    if (language != null) {
      attributes.put(LANGUAGE_ATTRIBUTE, StringUtils.toQuotedString(language));
    }

    if (associatedLanguage != null) {
      attributes.put(ASSOC_LANGUAGE_ATTRIBUTE, StringUtils.toQuotedString(associatedLanguage));
    }

    if (defaultRendition != null) {
      attributes.put(DEFAULT_ATTRIBUTE, defaultRendition.toString());
    }

    if (autoSelect != null) {
      attributes.put(AUTOSELECT_ATTRIBUTE, autoSelect.toString());
    }

    if (forced != null) {
      attributes.put(FORCED_ATTRIBUTE, forced.toString());
    }

    if (inStreamId != null) {
      attributes.put(INSTREAM_ID_ATTRIBUTE, StringUtils.toQuotedString(inStreamId));
    }

    return Tag.fromKV(
        TAG_KEY,
        new AttributeListTagValue(attributes)
    );
  }

  public String groupId() {
    return this.groupId;
  }

  public enum Confirmation {
    YES, NO
  }

  public enum Type {
    VIDEO("VIDEO"),
    AUDIO("AUDIO"),
    SUBTITLES("SUBTITLES"),
    CLOSED_CAPTIONS("CLOSED-CAPTIONS");

    final String value;


    Type(String value) {
      this.value = value;
    }
  }

  public static class Builder {

    private final String groupId;
    private final String name;
    private final MediaRendition.Type type;
    private URI uri;
    private String language;
    private String associatedLanguage;
    private Confirmation defaultRendition;
    private Confirmation autoSelect;
    private Confirmation forced;
    private String inStreamId;

    private Builder(
        @NotNull String groupId,
        @NotNull String name,
        @NotNull MediaRendition.Type type
    ) {
      this.groupId = Objects.requireNonNull(groupId, "groupId is null");
      this.name = Objects.requireNonNull(name, "name is null");
      this.type = Objects.requireNonNull(type, "type is null");
    }

    public Builder uri(URI uri) {
      this.uri = uri;
      return this;
    }

    public Builder language(String lang) {
      this.language = lang;
      return this;
    }

    public Builder associatedLanguage(String lang) {
      this.associatedLanguage = lang;
      return this;
    }

    public Builder defaultRendition() {
      this.defaultRendition = Confirmation.YES;
      return this;
    }

    public Builder forced() {
      this.forced = Confirmation.YES;
      return this;
    }

    public Builder autoSelect() {
      this.autoSelect = Confirmation.YES;
      return this;
    }

    public Builder inStreamId(String id) {
      this.inStreamId = id;
      return this;
    }

    public MediaRendition build() {
      return new MediaRendition(this);
    }
  }
}
