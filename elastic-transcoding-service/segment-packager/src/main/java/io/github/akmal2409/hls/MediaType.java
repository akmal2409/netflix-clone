package io.github.akmal2409.hls;

public enum MediaType {

  AUDIO_3GPP("audio/3gpp"),
  VIDEO_3GP("video/3gpp"),
  AUDIO_3GPP_2("audio/3gpp2"),
  VIDEO_3GPP_2("video/3gpp2"),

  AUDIO_MP4("audio/mp4"),
  VIDEO_MP4("video/mp4"),
  APPLICATION_MP4("application/mp4"),

  VIDEO_QUICKTIME("video/quicktime"),

  APPLICATION_MP21("application/mp21");

  final String value;

  MediaType(String value) {
    this.value = value;
  }


  public String getValue() {
    return value;
  }
}
