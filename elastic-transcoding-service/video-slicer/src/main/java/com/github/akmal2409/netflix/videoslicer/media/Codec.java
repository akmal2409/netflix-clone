package com.github.akmal2409.netflix.videoslicer.media;

/**
 * Following codec's codes are compatible with ffmpeg's definition.
 * Not all codecs were mapped.
 */
public enum Codec {

  SRT("srt", CodecType.SUBTITLES),
  SSA("ssa", CodecType.SUBTITLES),
  SUB_RIP("subrip", CodecType.SUBTITLES),
  WEB_VTT("webvtt", CodecType.SUBTITLES),
  ASS("ass", CodecType.SUBTITLES),
  TEXT("text", CodecType.SUBTITLES),



  TRUE_HD("truehd", CodecType.AUDIO),
  OPUS("opus", CodecType.AUDIO),
  MP3("mp3", CodecType.AUDIO),
  MP2("mp2", CodecType.AUDIO),
  MP1("mp1", CodecType.AUDIO),
  FLAC("flac", CodecType.AUDIO),
  EEAC3("eac3", CodecType.AUDIO),
  AAC("aac", CodecType.AUDIO),
  AC3("ac3", CodecType.AUDIO),
  VORBIS("vorbis", CodecType.AUDIO),


  MPEG4("mpeg4", CodecType.VIDEO),
  MPEG2_VIDEO("mpeg2video", CodecType.VIDEO),
  MPEG1_VIDEO("mpeg1video", CodecType.VIDEO),

  HEVC("hevc", CodecType.VIDEO),
  H_264("h264", CodecType.VIDEO),
  AV1("av1", CodecType.VIDEO),
  HDR("hdr", CodecType.VIDEO);

  private final String code;
  private final CodecType codecType;

  Codec(String code, CodecType codecType) {
    this.code = code;
    this.codecType = codecType;
  }


  public String code() {
    return this.code;
  }

  public CodecType codecType() {
    return this.codecType;
  }
}
