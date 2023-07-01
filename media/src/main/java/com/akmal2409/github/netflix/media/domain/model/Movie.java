package com.akmal2409.github.netflix.media.domain.model;

import com.akmal2409.github.netflix.media.domain.configuration.MediaS3Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Model representing a movie in the database, we don't need to store thumbnail, original banner or video keys
 * to S3 since those keys can be computed based on the movie ID and known prefixes.
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "movies")
@PrimaryKeyJoinColumn(name = "media_id")
public class Movie extends MediaContent implements VideoContent {

  @Column(name = "thumbnails_generated", nullable = false, columnDefinition = "BOOLEAN")
  private boolean thumbnailsGenerated;

  @Column(name = "media_transcoded", nullable = false, columnDefinition = "BOOLEAN")
  private boolean mediaTranscoded;

  @Column(name = "duration_seconds", columnDefinition = "INTEGER")
  private int durationSeconds;

  @Column(name = "available_from", columnDefinition = "TIMESTAMP")
  private Instant availableFrom;

  public Movie(UUID mediaId, String imdbId, String title, String description,
      LocalDate releaseDate, List<MediaProfessional> cast,
      List<Genre> genres, MediaContentType type, boolean newEntity,
      boolean thumbnailsGenerated, boolean mediaTranscoded, int durationSeconds,
      Instant availableFrom) {
    super(mediaId, imdbId, title, description, releaseDate, cast, genres, type, newEntity);
    this.thumbnailsGenerated = thumbnailsGenerated;
    this.mediaTranscoded = mediaTranscoded;
    this.durationSeconds = durationSeconds;
    this.availableFrom = availableFrom;
  }

  @Override
  public String bucketName() {
    return MediaS3Constants.BUCKET;
  }

  @Override
  public String objectKey() {
    return String.format(MediaS3Constants.MOVIE_ORIGINAL_FILE_PREFIX, this.mediaId.toString());
  }
}
