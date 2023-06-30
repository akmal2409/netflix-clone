package com.akmal2409.github.netflix.media.domain.service.internal;

import com.akmal2409.github.netflix.media.domain.model.Movie;
import com.akmal2409.github.netflix.media.domain.repository.GenreRepository;
import com.akmal2409.github.netflix.media.domain.repository.MovieRepository;
import com.akmal2409.github.netflix.media.infrastructure.dto.internal.MovieCreateCommand;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Internal Service implementation for mutations of movies intended only for internal employees.
 */
@Service("internalMovieService")
@RequiredArgsConstructor
public class MovieService {
  private final MovieRepository movieRepository;
  private final GenreRepository genreRepository;
  private final MediaProfessionalRepository professionalRepository;

  @Transactional(readOnly = true)
  public Optional<Movie> findById(UUID movieId) {
    return this.movieRepository.findByIdJoinGenres(movieId);
  }

  @Transactional(readOnly = true)
  public Page<Movie> findAll(int page, int size) {
    return this.movieRepository.findAll(PageRequest.of(page, size));
  }

  @Transactional
  public Movie create(MovieCreateCommand createCommand) {
    final var cast = this.professionalRepository.findAllByIds(createCommand.castIds());
    final var genres = this.genreRepository.findAllByIds(createCommand.genreIds());
    final var movie = createCommand.toMovie(cast, genres);
    movie.setMediaId(UUID.randomUUID());

    final var savedMovie = this.movieRepository.save(movie);
    savedMovie.setNewEntity(false);
    return savedMovie;
  }
}
