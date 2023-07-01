package com.akmal2409.github.netflix.media.domain.dto;

import com.akmal2409.github.netflix.media.domain.model.Genre;

public record GenreDto(
    int id,
    String name
) {

  public static GenreDto from(Genre genre) {
    return new GenreDto(
        genre.getId(),
        genre.getName()
    );
  }
}
