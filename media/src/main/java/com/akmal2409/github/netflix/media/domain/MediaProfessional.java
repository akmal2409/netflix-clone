package com.akmal2409.github.netflix.media.domain;

import jakarta.persistence.Entity;
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
public class Actor implements Persistable<UUID> {

  @Id
  private UUID id = UUID.randomUUID();
  private String imdbId;

  private String fullName;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "cast")
  private List<Movie> movies = new ArrayList<>();

  @Transient
  private boolean newEntity;

  @Override
  public boolean isNew() {
    return this.newEntity;
  }
}
