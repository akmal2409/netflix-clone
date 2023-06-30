package com.akmal2409.github.netflix.media.domain.repository;

import com.akmal2409.github.netflix.media.domain.model.Movie;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieRepository extends JpaRepository<Movie, UUID> {

  @Query("SELECT m FROM Movie m LEFT JOIN m.genres WHERE m.mediaId = :id")
  Optional<Movie> findByIdJoinGenres(@Param("id") UUID id);

  @Query("SELECT m FROM Movie m LEFT JOIN m.genres")
  List<Movie> findAllJoinGenres(Pageable pageable);
}
