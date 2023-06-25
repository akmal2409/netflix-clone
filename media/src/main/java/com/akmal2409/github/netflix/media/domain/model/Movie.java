package com.akmal2409.github.netflix.media.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Data;


/**
 * Model representing a movie in the database, we don't need to store thumbnail, original banner or video keys
 * to S3 since those keys can be computed based on the movie ID and known prefixes.
 */
@Entity
@Data
@Table(name = "movies")
@PrimaryKeyJoinColumn(name = "media_id")
public class Movie extends MediaContent {

  @Column(name = "thumbnails_generated", nullable = false, columnDefinition = "BOOLEAN")
  private boolean thumbnailsGenerated;

  @Column(name = "media_transcoded", nullable = false, columnDefinition = "BOOLEAN")
  private boolean mediaTranscoded;

  @Column(name = "duration_seconds", columnDefinition = "INTEGER")
  private int durationSeconds;

  @Column(name = "available_from", columnDefinition = "TIMESTAMP")
  private Instant availableFrom;

}
