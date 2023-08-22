package com.github.akmal2409.netflix.videoslicer.job.exception;

import java.util.UUID;

public class VideoDownloadException extends JobExecutionFailureException {

  public VideoDownloadException(String message, UUID jobId) {
    super(message, jobId);
  }

  public VideoDownloadException(String message, Throwable cause, UUID jobId) {
    super(message, cause, jobId);
  }
}
