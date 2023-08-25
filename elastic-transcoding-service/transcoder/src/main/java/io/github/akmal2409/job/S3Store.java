package io.github.akmal2409.job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileDownload;
import software.amazon.awssdk.transfer.s3.model.DirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.UploadDirectoryRequest;

/**
 * Abstraction over S3 AWS API to download files for the job and upload results
 */
public class S3Store {
  private static final Logger log = LoggerFactory.getLogger(S3Store.class);


  private final S3TransferManager transferManager;
  private final Path downloadDirectory;

  public S3Store(S3TransferManager transferManager, Path downloadDirectory) {
    this.transferManager = transferManager;
    this.downloadDirectory = downloadDirectory;
  }


  /**
   * Downloads files from S3 bucket and creates a directory with name {@code localDirectoryName} under {@link S3Store#downloadDirectory}
   * The desired files must be grouped under the same keyPrefix and will be downloaded into the same folder
   * @param bucket source S3 bucket where the files are located
   * @param keyPrefix common key prefix of a file
   * @param fileNames of the desired files
   * @param localDirectoryName directory name where to store multiple files
   * @return path to directory containing all requested files
   */
  public Path downloadSamePrefixFiles(String bucket, String keyPrefix, String localDirectoryName, String... fileNames) {

    final Path fileDownloadPath = this.downloadDirectory.resolve(localDirectoryName);
    try {
      Files.createDirectories(fileDownloadPath);
    } catch (IOException e) {
      throw new DownloadFailedException("Cannot create folder for the job " + fileDownloadPath);
    }

    final var downloadFutures = new ArrayList<CompletableFuture<CompletedFileDownload>>();

    log.debug("message=Starting download of files {} from bucket {} with keyPrefix {} to {};bucket={}",
        Arrays.toString(fileNames), bucket, keyPrefix, fileDownloadPath, bucket);


    for (String fileName: fileNames) {
      final var fileDestination = fileDownloadPath.resolve(fileName);
      final var fileKey = keyPrefix.concat("/").concat(fileName);

      final var downloadRequest = DownloadFileRequest.builder()
                                      .destination(fileDestination)
                                      .getObjectRequest(obj -> obj.bucket(bucket)
                                                                   .key(fileKey))
                                      .build();

      downloadFutures.add(transferManager.downloadFile(downloadRequest).completionFuture());
    }

    downloadFutures.forEach(CompletableFuture::join);
    return fileDownloadPath;
  }

  /**
   * Uploads directory with processed files such as segments, index file, audio etc. to the
   * destination bucket with a prefix. Files located at the top of the folder will have
   * {@code keyPrefix} all files in the nested directories will have a key: {@code keyPrefix} +
   * directories + fileName
   *
   * @param bucket    s3 bucket where the contents of the directory should be placed.
   * @param keyPrefix common key prefix for all the files.
   * @param directory path to upload.
   */
  public void uploadProcessedFiles(@NotNull String bucket, @NotNull String keyPrefix,
      @NotNull Path directory) {
    log.debug(
        "message=Starting upload of processed files from directory {} to bucket {} with key {};bucket={}",
        directory, bucket, keyPrefix, bucket);

    final var uploadRequest = UploadDirectoryRequest.builder()
                                  .followSymbolicLinks(false)
                                  .maxDepth(10)
                                  .s3Prefix(keyPrefix)
                                  .source(directory)
                                  .bucket(bucket)
                                  .build();

    final DirectoryUpload upload = this.transferManager.uploadDirectory(uploadRequest);

    try {
      upload.completionFuture().join();
      log.debug(
          "message=Finished upload of processed files from directory {} to bucket {} with key {};bucket={}",
          directory, bucket, keyPrefix, bucket);
    } catch (CompletionException e) {
      throw new UploadFailedException(
          String.format("Upload of directory %s to bucket %s with key prefix {} failed",
              directory, bucket, keyPrefix),
          e.getCause()
      );
    }
  }
}
