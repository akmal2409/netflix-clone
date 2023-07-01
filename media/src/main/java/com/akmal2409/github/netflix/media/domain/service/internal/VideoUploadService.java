package com.akmal2409.github.netflix.media.domain.service.internal;

import com.akmal2409.github.netflix.media.domain.configuration.MediaUploadConfigurationProperties;
import com.akmal2409.github.netflix.media.domain.dto.internal.VideoContentUploadRequest;
import com.akmal2409.github.netflix.media.domain.exception.UnsupportedVideoContentTypeException;
import com.akmal2409.github.netflix.media.domain.exception.VideoUploadFailedException;
import com.akmal2409.github.netflix.media.domain.model.VideoContent;
import java.net.URL;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoUploadService {
  public static final Map<String, String> contentTypeToExtension = Map.of(
      "video/mp4", "mp4",
      "video/webm", "webm"
  );

  private final S3Presigner s3Presigner;
  private final MediaUploadConfigurationProperties uploadProps;

  /**
   * Generates pre-signed upload URL to S3 for any media content implementing {@link VideoContent} interface.
   * Pre-signed URL validity is configured in {@link MediaUploadConfigurationProperties#getPreSignedUrlValidity()}.
   *
   * @param request upload request containing metadata of the file
   * @param content instance that supports video uploads
   * @return pre-signed URL with validity of {@link MediaUploadConfigurationProperties#getPreSignedUrlValidity()}
   */
  public PreSignedURL getSignedUploadUrl(VideoContentUploadRequest request, VideoContent content) {
    this.validateUploadRequest(request);

    try {
      log.debug("category=s3 action=PutObjectPresignRequest status=PENDING contentType={} contentLength={} key={} bucket={}",
          request.contentType(), request.contentLength(), content.objectKey(),
          content.bucketName());

      final var objectRequest = PutObjectRequest.builder()
                                    .bucket(content.bucketName())
                                    .key(content.objectKey().concat(".").concat(contentTypeToExtension.get(request.contentType())))
                                    .contentType(request.contentType())
                                    .contentLength(request.contentLength())
                                    .metadata(content.metadata())
                                    .build();

      final var preSignRequest = PutObjectPresignRequest.builder()
                                     .signatureDuration(uploadProps.getPreSignedUrlValidity())
                                     .putObjectRequest(objectRequest)
                                     .build();

      final var preSignedRequest = s3Presigner.presignPutObject(preSignRequest);

      log.info("category=s3 action=PutObjectPresignRequest status=SUCCESS contentType={} contentLength={} key={} bucket={}",
          request.contentType(), request.contentLength(), content.objectKey(),
          content.bucketName());

      return new PreSignedURL(preSignedRequest.url(), preSignedRequest.expiration());
    } catch (S3Exception e) {
      log.error("category=s3 action=PutObjectPresignRequest status=FAIL contentType={} contentLength={} key={} bucket={} error={}",
          request.contentType(), request.contentLength(), content.objectKey(),
          content.bucketName(), e.getMessage());
      throw new VideoUploadFailedException("Video upload URL generation failed", e);
    }
  }

  private void validateUploadRequest(VideoContentUploadRequest request) {
    if (!contentTypeToExtension.containsKey(request.contentType().toLowerCase())) {
      throw new UnsupportedVideoContentTypeException(request.contentType(), contentTypeToExtension.keySet());
    }

    if (request.contentLength() <= 0) {
      throw new VideoUploadFailedException("Invalid content length provided. Content Length: " + request.contentLength());
    }
  }
}
