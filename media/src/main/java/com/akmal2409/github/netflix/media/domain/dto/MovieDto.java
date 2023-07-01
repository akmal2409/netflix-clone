package com.akmal2409.github.netflix.media.domain.dto;

import com.akmal2409.github.netflix.media.domain.model.Movie;
import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

public record MovieDto(
    UUID mediaId,
    String title,
    String description,
    LocalDate releaseDate,
    Collection<GenreDto> genres,
    String thumbnailLocation,
    int durationSeconds) {

  public static MovieDto from(Movie movie) {
    return new MovieDto(
        movie.getMediaId(),
        movie.getTitle(),
        movie.getDescription(),
        movie.getReleaseDate(),
        movie.getGenres().stream().map(GenreDto::from).toList(),
        movie.getThumbnailLocation(),
        movie.getDurationSeconds()
    );
  }
}
