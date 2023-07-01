package com.akmal2409.github.netflix.media.domain.service.internal;

import java.net.URL;
import java.time.Instant;

public record PreSignedURL(URL url, Instant validUntil) {

}
