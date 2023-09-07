package io.github.akmal2409.hls;

public record HlsCodec(
    String codecId,
    byte profile,
    byte constraintSetFlags,
    byte level
) {


  @Override
  public String toString() {
    return String.format("%s.%02x%02x%02x", codecId, profile, constraintSetFlags, level);
  }

}
