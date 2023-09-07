package io.github.akmal2409.hls.codec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Codec {

  H264("h264", List.of(
      new Profile("Baseline", 66),
      new Profile("Constrained Baseline", 66),
      new Profile("Main", 77),
      new Profile("Extended", 88),
      new Profile("High", 100),
      new Profile("High 10", 110),
      new Profile("High 10 Intra", 110),
      new Profile("High 4:2:2", 122),
      new Profile("High 4:2:2 Intra", 122),
      new Profile("High 4:4:4", 144),
      new Profile("High 4:4:4 Predictive", 244),
      new Profile("High 4:4:4 Intra", 244),
      new Profile("CAVLC 4:4:4", 44),
      new Profile("Multiview High", 118),
      new Profile("Stereo High", 128)
  ), Type.VIDEO),
  HEVC("hevc", List.of(
      new Profile("4:2:2", 244),
      new Profile("High", 1),
      new Profile("Spatially Scalable", 5),
      new Profile("SNR Scalable", 6),
      new Profile("Main", 1),
      new Profile("Simple", 0)
  ), Type.VIDEO),
  MP2("mp2", List.of(), Type.AUDIO),
  MP3("mp3", List.of(), Type.AUDIO),
  AAC("aac", List.of(
      new Profile("LC", 1),
      new Profile("HE-AAC", 4),
      new Profile("HE-AACv2", 28),
      new Profile("LD", 22),
      new Profile("ELD", 38),
      new Profile("Main", 0),
      new Profile("SSR", 2),
      new Profile("LTP", 3)
  ), Type.AUDIO),
  AC3("ac3", List.of(), Type.AUDIO),
  EAC3("eac3", List.of(), Type.AUDIO);

  private static final Map<String, Codec> CACHE = new HashMap<>();

  static {
    for (Codec codec : values()) {
      CACHE.put(codec.id, codec);
    }
  }

  final String id;
  final Map<String, Profile> profiles;
  final Type type;


  Codec(String id, List<Profile> profiles, Type type) {
    this.id = id;
    this.profiles = profiles.stream()
                        .collect(Collectors.toMap(
                            Profile::key,
                            Function.identity(),
                            (p1, p2) -> p1
                        ));
    this.type = type;
  }

  public static Optional<Codec> byId(String id) {
    return Optional.ofNullable(CACHE.get(id));
  }

  public Optional<Profile> getProfileByKey(String key) {
    return Optional.ofNullable(profiles.get(key));
  }

  public String id() {
    return this.id;
  }

  public Type type() {
    return this.type;
  }

  public enum Type {
    AUDIO, VIDEO
  }
}
