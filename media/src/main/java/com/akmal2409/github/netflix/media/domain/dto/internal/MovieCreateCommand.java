package com.akmal2409.github.netflix.media.domain.dto.internal;

import com.akmal2409.github.netflix.media.domain.model.Genre;
import com.akmal2409.github.netflix.media.domain.model.MediaContentType;
import com.akmal2409.github.netflix.media.domain.model.MediaProfessional;
import com.akmal2409.github.netflix.media.domain.model.Movie;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MovieCreateCommand(
  String imdbId,
  @NotEmpty(message = "title is required") String title,
  @NotEmpty(message = "description is required") String description,
  @NotNull(message = "releaseDate is required") LocalDate releaseDate,
  @NotNull(message = "genreIds list cannot be null") List<Integer> genreIds,
  @NotNull(message = "castIds list cannot be null") List<UUID> castIds,
  Instant availableFrom
) {

  /**
   * Maps newly created movie's fields to a new movie domain entity.
   *
   * @param cast based on the castIds
   * @param genres based on the genreIds
   * @return mapped {@link Movie} entity.
   */
  public Movie toMovie(List<MediaProfessional> cast, List<Genre> genres) {
    final var movie = new Movie(null, imdbId, title,
        description, releaseDate, cast, genres, MediaContentType.MOVIE, true, false,
        false, -1, availableFrom);

    movie.getCast().forEach(person -> person.getMovies().add(movie));
    movie.getGenres().forEach(genre -> genre.getMovies().add(movie));

    return movie;
  }
}
