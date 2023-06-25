package com.akmal2409.github.netflix.media.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.Persistable;

@Entity
@AllArgsConstructor
@Data
@NoArgsConstructor
@Table(name = "episodes")
public class Episode implements Persistable<UUID> {

  @Id
  @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
  private UUID id = UUID.randomUUID();

  @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(100)")
  private String title;

  @Column(name = "description", nullable = false, columnDefinition = "VARCHAR(512)")
  private String description;

  @Column(name = "release_date", nullable = false, columnDefinition = "DATE")
  private LocalDate releaseDate;

  @Column(name = "available_from", columnDefinition = "TIMESTAMP")
  private Instant availableFrom;

  @Column(name = "thumbnails_generated", nullable = false, columnDefinition = "BOOLEAN")
  private boolean thumbnailsGenerated;

  @Column(name = "media_transcoded", nullable = false, columnDefinition = "BOOLEAN")
  private boolean mediaTranscoded;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "episode_collection_id", referencedColumnName = "id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private EpisodeCollection episodeCollection;

  @Transient
  private boolean newEntity;

  @Override
  public boolean isNew() {
    return this.newEntity;
  }
}
