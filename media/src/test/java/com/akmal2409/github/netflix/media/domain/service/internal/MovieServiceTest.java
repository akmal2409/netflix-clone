package com.akmal2409.github.netflix.media.domain.service.internal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.akmal2409.github.netflix.media.domain.model.Genre;
import com.akmal2409.github.netflix.media.domain.model.MediaContentType;
import com.akmal2409.github.netflix.media.domain.model.MediaOccupation;
import com.akmal2409.github.netflix.media.domain.model.MediaProfessional;
import com.akmal2409.github.netflix.media.domain.model.Movie;
import com.akmal2409.github.netflix.media.domain.repository.GenreRepository;
import com.akmal2409.github.netflix.media.domain.repository.MediaProfessionalRepository;
import com.akmal2409.github.netflix.media.domain.repository.MovieRepository;
import com.akmal2409.github.netflix.media.domain.dto.internal.MovieCreateCommand;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

  @Mock
  MovieRepository movieRepository;

  @Mock
  GenreRepository genreRepository;

  @Mock
  MediaProfessionalRepository professionalRepository;

  @Captor
  ArgumentCaptor<Movie> movieCaptor;

  @InjectMocks
  MovieService movieService;

  @Test
  @DisplayName("Should create movie with cast and genres when ids are present")
  void shouldCreateMovieWithCastAndGenresWhenIdsPresent() {
    final var expectedCast = List.of(
        new MediaProfessional(UUID.randomUUID(), "imdfa3", "name", MediaOccupation.ACTOR, new ArrayList<>(),
            false)
    );

    final var expectedGenres = List.of(new Genre(1, "name", new ArrayList<>()));

    final var expectedMovie = new Movie(null, "imdb343", "title", "desc", LocalDate.EPOCH,
        expectedCast, expectedGenres, MediaContentType.MOVIE, true, false, false, -1, Instant.EPOCH);

    final var createCommand = new MovieCreateCommand(expectedMovie.getImdbId(), expectedMovie.getTitle(),
        expectedMovie.getDescription(), expectedMovie.getReleaseDate(), expectedGenres.stream().map(Genre::getId).toList(),
        expectedCast.stream().map(MediaProfessional::getId).toList(), expectedMovie.getAvailableFrom());

    when(genreRepository.findAllByIds(anyList())).thenReturn(expectedGenres);
    when(professionalRepository.findAllByIds(anyList())).thenReturn(expectedCast);
    when(movieRepository.save(any(Movie.class))).thenReturn(expectedMovie);

    movieService.create(createCommand);

    verify(movieRepository).save(movieCaptor.capture());

    assertThat(movieCaptor.getValue())
        .usingRecursiveComparison()
        .ignoringFields("mediaId", "newEntity")
        .isEqualTo(expectedMovie);
  }
}
