package io.github.akmal2409.hls;

import io.github.akmal2409.PlaylistUtils;
import io.github.akmal2409.hls.codec.Codec;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class MasterPlaylist implements Playlist {

  final int version;
  final List<VariantStream> variantStreams;
  final Map<String, List<MediaRendition>> renditionGroups;

  private MasterPlaylist(Builder builder) {
    this.version = builder.version;
    this.renditionGroups = builder.renditions
                               .entrySet().stream()
                               .collect(Collectors.toMap(
                                   Map.Entry::getKey,
                                   entry -> Collections.unmodifiableList(entry.getValue())
                               ));
    this.variantStreams = Collections.unmodifiableList(builder.variantStreams);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static void main(String[] args) {
    final var master = MasterPlaylist.builder()
                           .rendition(
                               MediaRendition.builder(
                                       "500kbs-video", "500kbs Angle 1", MediaRendition.Type.VIDEO
                                   )
                                   .defaultRendition()
                                   .autoSelect()
                                   .uri(URI.create("Angle1/500kbs/index.m3u8"))
                                   .build()
                           )
                           .rendition(
                               MediaRendition.builder(
                                       "500kbs-video", "500kbs Angle 2", MediaRendition.Type.VIDEO
                                   )
                                   .autoSelect()
                                   .uri(URI.create("Angle2/500kbs/index.m3u8"))
                                   .build()
                           )
                           .rendition(
                               MediaRendition.builder(
                                       "300kbs-video", "300kbs Angle 1", MediaRendition.Type.VIDEO
                                   )
                                   .defaultRendition()
                                   .autoSelect()
                                   .uri(URI.create("Angle1/300kbs/index.m3u8"))
                                   .build()
                           )
                           .rendition(
                               MediaRendition.builder(
                                       "300kbs-video", "300kbs Angle 2", MediaRendition.Type.VIDEO
                                   )
                                   .autoSelect()
                                   .uri(URI.create("Angle2/300kbs/index.m3u8"))
                                   .build()
                           )
                           .rendition(
                               MediaRendition.builder(
                                       "320kbps-aac", "320kbps AAC audio", MediaRendition.Type.AUDIO
                                   )
                                   .defaultRendition()
                                   .autoSelect()
                                   .language("eng")
                                   .associatedLanguage("de")
                                   .uri(URI.create("audio/320kbps/index.m3u8"))
                                   .build()
                           )
                           .rendition(
                               MediaRendition.builder(
                                       "subtitles", "English subtitles", MediaRendition.Type.SUBTITLES
                                   )
                                   .defaultRendition()
                                   .autoSelect()
                                   .language("eng")
                                   .uri(URI.create("audio/subtitles/en-index.m3u8"))
                                   .build()
                           )
                           .rendition(
                               MediaRendition.builder(
                                       "subtitles", "German subtitles", MediaRendition.Type.SUBTITLES
                                   )
                                   .defaultRendition()
                                   .autoSelect()
                                   .language("de")
                                   .uri(URI.create("audio/subtitles/de-index.m3u8"))
                                   .build()
                           )
                           .rendition(
                               MediaRendition.builder(
                                       "cc", "English cc", MediaRendition.Type.CLOSED_CAPTIONS
                                   )
                                   .defaultRendition()
                                   .autoSelect()
                                   .language("eng")
                                   .uri(URI.create("audio/cc/eng-index.m3u8"))
                                   .build()
                           )
                           .variant(builder ->
                                        builder
                                            .bandwidth(440000)
                                            .codec(new HlsCodec("avc1",
                                                (byte) Codec.H264.getProfileByKey("High").get()
                                                           .code(),
                                                (byte) 0, (byte) 30))
                                            .audioRenditionGroup("320kbps-aac")
                                            .videoRenditionGroup("500kbs-video")
                                            .closedCaptionsRenditionGroup("cc")
                                            .subtitlesRenditionGroup("subtitles")
                                            .playlistUri(URI.create("Angle1/500kbs/index.m3u8"))
                                            .resolution(new Resolution(1920, 1080))
                           )
                           .variant(builder ->
                                        builder
                                            .bandwidth(320000)
                                            .codec(new HlsCodec("avc1",
                                                (byte) Codec.H264.getProfileByKey("High").get()
                                                           .code(),
                                                (byte) 0, (byte) 30))
                                            .audioRenditionGroup("320kbps-aac")
                                            .videoRenditionGroup("300kbs-video")
                                            .closedCaptionsRenditionGroup("cc")
                                            .subtitlesRenditionGroup("subtitles")
                                            .playlistUri(URI.create("Angle1/300kbs/index.m3u8"))
                                            .resolution(new Resolution(1280, 720))
                           )
                           .build();

    try (final var chan = Files.newByteChannel(Path.of("./master.m3u8"),
        StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

      chan.write(ByteBuffer.wrap(
          master.serialise().getBytes(StandardCharsets.UTF_8)
      ));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String serialise() {
    final var builder = new StringBuilder();

    PlaylistUtils.writeCommonHeader(builder, this.version);

    builder.append("\n\n");

    // write media renditions, order doesn't matter, but best to keep consistent
    for (Map.Entry<String, List<MediaRendition>> mediaGroup : this.renditionGroups.entrySet()) {

      for (MediaRendition rendition : mediaGroup.getValue()) {

        builder.append(rendition.toTag().serialise());
        builder.append("\n");
      }
      builder.append("\n");
    }

    builder.append("\n");

    // write stream variants
    for (VariantStream variant : this.variantStreams) {
      builder.append(variant.toTag().serialise());
      builder.append("\n");
      builder.append("\n");
    }

    return builder.toString();
  }

  @Override
  public Type type() {
    return Type.MASTER;
  }

  public static class Builder {

    private final List<VariantStream> variantStreams = new ArrayList<>();
    private final Map<String, ArrayList<MediaRendition>> renditions = new HashMap<>();
    private int version = 1;

    public Builder rendition(@NotNull MediaRendition rendition) {
      this.renditions.computeIfAbsent(rendition.groupId, k -> new ArrayList<>())
          .add(rendition);

      return this;
    }

    public Builder variant(@NotNull Consumer<VariantStream.Builder> builderConsumer) {
      final var builder = VariantStream.builder();
      builderConsumer.accept(builder);

      this.variantStreams.add(builder.build());
      return this;
    }

    public Builder version(int version) {
      this.version = version;
      return this;
    }

    public MasterPlaylist build() {
      return new MasterPlaylist(this);
    }
  }
}
