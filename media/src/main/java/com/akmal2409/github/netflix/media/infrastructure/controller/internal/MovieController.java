package com.akmal2409.github.netflix.media.infrastructure.controller.internal;

import com.akmal2409.github.netflix.media.domain.dto.internal.VideoContentUploadRequest;
import com.akmal2409.github.netflix.media.domain.service.internal.MovieService;
import com.akmal2409.github.netflix.media.domain.dto.MovieDto;
import com.akmal2409.github.netflix.media.domain.dto.Page;
import com.akmal2409.github.netflix.media.domain.dto.internal.MovieCreateCommand;
import com.akmal2409.github.netflix.media.domain.service.internal.PreSignedURL;
import com.akmal2409.github.netflix.media.domain.service.internal.VideoUploadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.net.URL;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API endpoint handler for administrators to view movies that are in draft stage and perform modifications.
 */
@RestController("internalMovieController")
@RequestMapping(MovieController.BASE_PATH)
@RequiredArgsConstructor
public class MovieController {

  public static final String BASE_PATH = "/internal/v1/movies";
  private final MovieService movieService;
  private final VideoUploadService videoUploadService;

  /**
   * Returns existing movie by ID regardless of its general availability.
   * @param movieId {@link UUID} of a movie
   * @return {@link MovieDto}
   */
  @GetMapping("/{movieId}")
  public ResponseEntity<MovieDto> findById(@PathVariable UUID movieId) {
    return movieService.findById(movieId)
               .map(MovieDto::from)
               .map(ResponseEntity::ok)
               .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public Page<MovieDto> findAll(@RequestParam(defaultValue = "0") @Valid @Min(0) int page,
      @RequestParam(defaultValue = "25") @Valid @Min(1) int size) {
    return Page.from(movieService.findAll(page, size), MovieDto::from);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MovieDto create(@RequestBody @Valid MovieCreateCommand createCommand) {
    return MovieDto.from(this.movieService.create(createCommand));
  }

  @PostMapping("/{movieId}/upload-url")
  public ResponseEntity<PreSignedURL> generateUploadUrl(@PathVariable @Valid @NotNull UUID movieId,
      @RequestBody @Valid VideoContentUploadRequest uploadRequest) {
    return movieService.findById(movieId)
               .map(movie -> videoUploadService.getSignedUploadUrl(
                   uploadRequest,
                   movie
               ))
               .map(ResponseEntity::ok)
               .orElse(ResponseEntity.notFound().build());
  }
}
