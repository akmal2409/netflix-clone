package com.akmal2409.github.netflix.media.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.Persistable;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "episode_collections")
public class EpisodeCollection implements Persistable<UUID> {

  @Id
  @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
  private UUID id = UUID.randomUUID();

  @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(100)")
  private String title;

  @Column(name = "description", nullable = false, columnDefinition = "description")
  private String description;

  @Column(name = "thumbnails_generated", nullable = false, columnDefinition = "BOOLEAN")
  private boolean thumbnailsGenerated;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "series_id", referencedColumnName = "media_id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Series series;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "episodeCollection")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<Episode> episodes = new ArrayList<>();


  @Transient
  private boolean newEntity;

  @Override
  public boolean isNew() {
    return this.newEntity;
  }
}
