package com.akmal2409.github.netflix.media.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
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

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MediaProfessional implements Persistable<UUID> {

  @Id
  @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
  private UUID id = UUID.randomUUID();

  @Column(name = "imdb_id", columnDefinition = "VARCHAR(30)")
  private String imdbId;

  @Column(name = "full_name", nullable = false, columnDefinition = "VARCHAR(70)")
  private String fullName;

  @Column(name = "occupation", nullable = false, columnDefinition = "VARCHAR(50)")
  @Enumerated(EnumType.STRING)
  private MediaOccupation occupation;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "cast")
  private List<MediaContent> movies = new ArrayList<>();

  @Transient
  private boolean newEntity;

  @Override
  public boolean isNew() {
    return this.newEntity;
  }
}
