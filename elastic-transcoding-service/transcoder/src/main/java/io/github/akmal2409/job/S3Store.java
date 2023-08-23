package io.github.akmal2409.job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileDownload;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;

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
  public Path downloadSamePrefixFiles(String bucket, String keyPrefix, String[] fileNames,
      String localDirectoryName) {

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
}
