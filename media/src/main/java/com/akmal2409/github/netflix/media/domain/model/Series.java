package com.akmal2409.github.netflix.media.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "series")
@PrimaryKeyJoinColumn(name = "media_id")
public class Series extends MediaContent {


  @Column(name = "thumbnails_generated", nullable = false, columnDefinition = "BOOLEAN")
  private boolean thumbnailsGenerated;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "series")
  private List<EpisodeCollection> episodeCollections = new ArrayList<>();
}
