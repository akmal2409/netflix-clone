package com.github.akmal2409.netflix.videoslicer.job;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.github.akmal2409.netflix.videoslicer.job.exception.DuplicateJobException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

@ExtendWith(MockitoExtension.class)
class S3VideoStoreTest {

  @Mock
  S3TransferManager transferManager;


  @Test
  @DisplayName("Will throw DuplicateJobException if the job directory already exists")
  void throwsDuplicateJobExceptionWhenDownloadingExistingJob() throws IOException {
    final var temporaryFolder = Files.createTempDirectory(null);

    final var jobId = UUID.randomUUID();

    Files.createDirectory(temporaryFolder.resolve(jobId.toString()));

    final var store = new S3Store(temporaryFolder, transferManager);

    assertThatThrownBy(() -> store.downloadSource(jobId, "test", "test"))
        .isInstanceOf(DuplicateJobException.class);
  }


}
