package com.github.akmal2409.netflix.videoslicer.media;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public enum MediaContainer {

  MKV(container -> container
                       .addExtension("mkv").addExtension("mk3d").addExtension("mka").addExtension("mks")

                       .addCodec(Codec.MPEG4).addCodec(Codec.MPEG1_VIDEO).addCodec(Codec.MPEG2_VIDEO)
                       .addCodec(Codec.H_264).addCodec(Codec.HEVC)
                       .addCodec(Codec.HDR)
                       .addCodec(Codec.AV1)

                       .addCodec(Codec.MP3).addCodec(Codec.MP2).addCodec(Codec.MP1)
                       .addCodec(Codec.AC3).addCodec(Codec.EEAC3)
                       .addCodec(Codec.VORBIS)
                       .addCodec(Codec.FLAC)
                       .addCodec(Codec.AAC)
                       .addCodec(Codec.TRUE_HD)
                       .addCodec(Codec.OPUS)

                       .addCodec(Codec.TEXT).addCodec(Codec.SSA).addCodec(Codec.WEB_VTT).addCodec(Codec.ASS).addCodec(Codec.SRT)
    );

  private final Codec[] supportedCodecs;
  private final String[] fileExtensions;

  private static final Map<Codec, List<MediaContainer>> CODEC_TO_CONTAINER_MAPPING = new ConcurrentHashMap<>();

  static {
    // initialize mapping to have quicker lookups
    for (MediaContainer container: values()) {
      for (Codec codec: container.supportedCodecs) {
        CODEC_TO_CONTAINER_MAPPING.computeIfAbsent(codec, k -> new ArrayList<>())
                                      .add(container);
      }
    }
  }

  MediaContainer(Consumer<Builder> customizer) {
    final var builder = new Builder();
    customizer.accept(builder);

    this.supportedCodecs = builder.codecs.toArray(new Codec[0]);
    this.fileExtensions = builder.fileExtensions.toArray(new String[0]);
  }

  public static Optional<MediaContainer> getFirstSuitableContainer(Codec codec) {
    final var containers = CODEC_TO_CONTAINER_MAPPING.get(codec);

    if (containers == null || containers.isEmpty()) return Optional.empty();

    return Optional.of(containers.get(0));
  }

  public static class Builder {
    private final List<String> fileExtensions;
    private final List<Codec> codecs;

    private Builder() {
      this.fileExtensions = new ArrayList<>();
      this.codecs = new ArrayList<>();
    }

    public Builder addExtension(String ext) {
      this.fileExtensions.add(ext);
      return this;
    }

    public Builder addCodec(Codec codec) {
      this.codecs.add(codec);
      return this;
    }
  }

  public String[] fileExtensions() {
    return this.fileExtensions;
  }

  public Codec[] supportedCodecs() {
    return this.supportedCodecs;
  }
}
