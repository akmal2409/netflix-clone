package io.github.akmal2409.hls;

public record Resolution(
    int width,
    int height
) {

  @Override
  public String toString() {
    return String.format("%dx%d", width, height);
  }
}
