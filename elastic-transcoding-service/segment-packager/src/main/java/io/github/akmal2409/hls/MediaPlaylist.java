package io.github.akmal2409.hls;

import io.github.akmal2409.PlaylistUtils;
import io.github.akmal2409.hls.tag.Tag;
import io.github.akmal2409.hls.tag.TagValue.SingleTagValue;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Optional tags will be stored in a LinkedHashMap
 */
public class MediaPlaylist implements Playlist {

  /**
   * #EXT-X-VERSION:<code>n</code>
   * TODO: add docs
   */
  final int version;
  /**
   * EXT-X-TARGETDURATION:<code>duration</code>> tag - floating point duration in seconds (floating
   * since version 3) https://datatracker.ietf.org/doc/html/rfc8216#section-4.3.3.1
   */
  final float targetDuration;

  /**
   * Ordered set containing header tags (all tags that come before the media segments). Tags must be
   * the ones from {@link MediaPlaylistTags}
   */
  final Set<Tag> headerTags;
  /**
   * List of ordered sets that contains tags per each media segment
   */
  final List<Set<Tag>> mediaTags;

  MediaPlaylist(Builder builder) {
    this.version = builder.version;
    this.targetDuration = builder.targetDuration;
    this.headerTags = builder.headerTags;
    this.mediaTags = builder.mediaTags;
  }

  public static void main(String[] args) {
    final var playlist = new Builder()
                             .version(6)
                             .targetDuration(5.005f)

                             .playlistType(Type.VOD)
                             .iFramesOnly()
                             .mediaSequence(0)

                             .mediaSegment(5.005f, URI.create("720p/segment-0.ts"))
                             .mediaSegment(5.005f, URI.create("720p/segment-1.ts"))
                             .mediaSegment(3.005f, URI.create("720p/segment-2.ts"))
                             .build();

    System.out.println(playlist);
  }

  @Override
  public String serialise() {
    final var builder = new StringBuilder();

    PlaylistUtils.writeCommonHeader(builder, this.version);

    builder.append(
        Tag.fromKV(MediaPlaylistTags.TARGET_DURATION,
                new SingleTagValue(String.valueOf(this.targetDuration)))
            .serialise()
    );

    builder.append("\n\n");

    // append header tags of a media playlist
    for (Tag headerTag : this.headerTags) {
      builder.append(headerTag.serialise());
      builder.append("\n");
    }

    // append media tags of a media playlist, i.e. segments for example
    for (Set<Tag> tags : this.mediaTags) {
      for (Tag tag : tags) {
        builder.append(tag.serialise());
        builder.append("\n");
      }

      builder.append("\n");
    }

    return builder.toString();
  }

  @Override
  public Playlist.Type type() {
    return Playlist.Type.MEDIA;
  }

  @Override
  public String toString() {
    return this.serialise();
  }

  public enum Type {
    VOD, EVENT
  }

  public static class Builder {

    private Float targetDuration; // required to specify
    private Integer version;
    private final Set<Tag> headerTags = new LinkedHashSet<>();
    private final List<Set<Tag>> mediaTags = new ArrayList<>();


    public Builder targetDuration(float duration) {
      this.targetDuration = duration;
      return this;
    }

    public Builder version(int version) {
      if (version < 1 || version > 6) {
        throw new IllegalArgumentException("Version can be only between 1 and 6");
      }
      this.version = version;
      return this;
    }

    public Builder mediaSequence(int number) {
      this.headerTags.add(
          Tag.fromKV(MediaPlaylistTags.MEDIA_SEQUENCE, new SingleTagValue(String.valueOf(number))));
      return this;
    }

    public Builder discontinuitySequence(int number) {
      this.headerTags.add(Tag.fromKV(MediaPlaylistTags.DISCONTINUITY_SEQUENCE,
          new SingleTagValue(String.valueOf(number))));
      return this;
    }

    public Builder playlistType(Type type) {
      this.headerTags.add(
          Tag.fromKV(MediaPlaylistTags.PLAYLIST_TYPE_TAG, new SingleTagValue(type.toString())));
      return this;
    }

    public Builder iFramesOnly() {
      this.headerTags.add(Tag.fromKey(MediaPlaylistTags.I_FRAMES_ONLY));
      return this;
    }


    public Builder mediaSegment(float duration, URI uri) {
      final var tags = new LinkedHashSet<Tag>();

      tags.add(Tag.fromKV(MediaPlaylistTags.EXTINF, new SingleTagValue(duration + ",")));
      tags.add(Tag.fromUri(uri));

      this.mediaTags.add(tags);

      return this;
    }

    public MediaPlaylist build() {
      this.mediaTags.add(Set.of(Tag.fromKey(MediaPlaylistTags.END_LIST_TAG)));

      return new MediaPlaylist(this);
    }
  }
}
