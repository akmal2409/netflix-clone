package com.github.akmal2409.netflix.videoslicer.job;

import com.github.akmal2409.netflix.videoslicer.job.exception.DuplicateJobException;
import com.github.akmal2409.netflix.videoslicer.job.exception.VideoDownloadException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileDownload;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.FileDownload;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

/**
 * Class containing required operations for carrying out a preprocessing job such as downloading the
 * source video file and uploading preprocessed files.
 */
public class S3VideoStore {
  private static final Logger log = LoggerFactory.getLogger(S3VideoStore.class);

  /**
   * Storage folder that keeps the source files. {videoFolder}/{jobId}/{filename}.{extension}
   */
  private final Path videoFolder;
  private final S3TransferManager s3TransferManager;

  public S3VideoStore(@NotNull Path videoFolder,
      @NotNull S3TransferManager s3TransferManager) {
    this.videoFolder = videoFolder;
    this.s3TransferManager = s3TransferManager;
  }

  /**
   * Downloads source video file to the folder on disk and returns the path to the file.
   *
   * @throws VideoDownloadException if the download failed or preparation for download
   * @throws DuplicateJobException if the contents cannot be stored because this job has associated files on disk.
   * @param jobId  of the transcoding job.
   * @param bucket s3 bucket name.
   * @param key    file key.
   * @return path to the file.
   */
  public JobVideoSource downloadSource(@NotNull UUID jobId, @NotNull String bucket, @NotNull String key) {
    final String fileName = extractFileNameFromS3Key(key);

    Path jobDirectory;
    log.debug("message=Preparing to download video;jobId={};bucket={};file={}",
        jobId, bucket, key);
    try {
      jobDirectory = createJobDirectoryOrElseFail(jobId);
    } catch (IOException e) {
      throw new VideoDownloadException("Cannot set up folder", e, jobId);
    }

    final var filePath = jobDirectory.resolve(fileName);

    final FileDownload download = s3TransferManager
                                                 .downloadFile(
                                                     DownloadFileRequest.builder()
                                                         .getObjectRequest(b -> b.bucket(bucket).key(key))
                                                         .destination(filePath)
                                                         .addTransferListener(
                                                             LoggingTransferListener.create())
                                                         .build());

    try {
      final CompletedFileDownload result = download.completionFuture().join();
      log.debug("message=Downloaded file successfully;jobId={};bucket={};file={};location={}",
          jobId, bucket, key, jobDirectory);

      return new JobVideoSource(
          filePath,
          fileName,
          result.response().contentLength()
      );
    } catch (CancellationException e) {
      throw new VideoDownloadException("Download failed because it was cancelled", e, jobId);
    } catch (CompletionException e) {
      throw new VideoDownloadException("Download failed due to exception", e.getCause(), jobId);
    }
  }

  private String extractFileNameFromS3Key(String key) {
    int lastSlashIndex = -1;

    // will ignore trailing slash (that is why lastIndex is not used)
    for (int i = key.length() - 2; i >= 0; i--) {
      if (key.charAt(i) == '/') {
        lastSlashIndex = i;
        break;
      }
    }

    if (lastSlashIndex != -1) {
      return key.substring(lastSlashIndex + 1);
    } else {
      return key; // means no slashes and we have a full filename
    }
  }

  private Path createJobDirectoryOrElseFail(UUID jobId) throws IOException {
    final var jobFileFolder = videoFolder.resolve(jobId.toString());

    synchronized (S3VideoStore.class) {
      if (Files.exists(jobFileFolder)) {
        log.error("message=Duplicate job detected;jobId={}", jobId);
        throw new DuplicateJobException("Cannot create directory for a job because it already exists", jobId);
      } else {
        Files.createDirectories(jobFileFolder);
      }
    }

    return jobFileFolder;
  }
}
