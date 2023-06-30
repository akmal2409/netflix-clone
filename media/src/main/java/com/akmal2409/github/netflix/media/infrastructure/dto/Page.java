package com.akmal2409.github.netflix.media.infrastructure.dto;

import java.util.List;
import java.util.function.Function;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;

public record Page<T>(
    List<T> content,
    boolean hasNext,
    boolean hasPrev,
    int page,
    long totalItems,
    int totalPages,
    int itemsPerPage
) {

  public static <IN, OUT> Page<OUT> from(@NonNull org.springframework.data.domain.Page<IN> detailedPage,
      @NonNull Function<IN, OUT> mappingFn) {
    return new Page<>(
        detailedPage.getContent().stream().map(mappingFn).toList(),
        detailedPage.hasNext(),
        detailedPage.hasPrevious(),
        detailedPage.getNumber(),
        detailedPage.getTotalElements(),
        detailedPage.getTotalPages(),
        detailedPage.getSize()
    );
  }
}
