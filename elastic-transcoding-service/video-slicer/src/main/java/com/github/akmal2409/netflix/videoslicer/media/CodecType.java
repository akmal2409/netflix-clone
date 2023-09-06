package com.github.akmal2409.netflix.videoslicer.media;

import java.util.Optional;

/**
 * Following string enum values are compatible with ffmpeg's codec types (codec_type property)
 */
public enum CodecType {

  VIDEO("Video"), AUDIO("Audio"), SUBTITLES("Subtitles");

  private final String value;

  CodecType(String value) {
    this.value = value;
  }

  public static Optional<CodecType> fromValue(String value) {
    for (CodecType type: values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return Optional.of(type);
      }
    }

    return Optional.empty();
  }

  public String value() {
    return this.value;
  }
}
