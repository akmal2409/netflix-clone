package com.akmal2409.github.netflix.media.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.Persistable;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "media_contents")
public abstract class MediaContent implements Persistable<UUID> {

  @Id
  @Column(name = "id", nullable = false, columnDefinition = "uuid")
  protected UUID mediaId = UUID.randomUUID();

  @Column(name = "imdb_id", columnDefinition = "VARCHAR(30)")
  protected String imdbId;

  @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(100)")
  protected String title;

  @Column(name = "description", nullable = false, columnDefinition = "VARCHAR(512)")
  protected String description;

  @Column(name = "release_date", nullable = false, columnDefinition = "DATE")
  private LocalDate releaseDate;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "media_cast",
      joinColumns = @JoinColumn(name = "media_id", referencedColumnName = "id", columnDefinition = "uuid"),
      inverseJoinColumns = @JoinColumn(name = "actor_id", referencedColumnName = "id", columnDefinition = "uuid")
  )
  private List<MediaProfessional> cast = new ArrayList<>();

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "media_genres",
      joinColumns = @JoinColumn(name = "media_id", referencedColumnName = "id", columnDefinition = "uuid"),
      inverseJoinColumns = @JoinColumn(name = "genre_id", referencedColumnName = "id", columnDefinition = "INTEGER")
  )
  private List<Genre> genres = new ArrayList<>();


  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, updatable = false, columnDefinition = "VARCHAR(30)")
  private MediaContentType type;

  @Transient
  protected boolean newEntity = false;

  @Override
  public boolean isNew() {
    return this.newEntity;
  }

  @Override
  public UUID getId() {
    return this.mediaId;
  }

  /**
   * Returns S3 location without domain name (only path) to the thumbnails
   */
  public String getThumbnailLocation() {
    return null;
  }
}
