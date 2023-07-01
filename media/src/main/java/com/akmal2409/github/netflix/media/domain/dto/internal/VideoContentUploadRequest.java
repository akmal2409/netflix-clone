package com.akmal2409.github.netflix.media.domain.dto.internal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record VideoContentUploadRequest(
    @Min(value = 1, message = "contentLength must be at least 1 byte") long contentLength,
    @NotEmpty(message = "contentType is required") String contentType
) {

}
