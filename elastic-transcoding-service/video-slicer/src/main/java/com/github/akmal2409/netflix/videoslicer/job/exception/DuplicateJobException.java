package com.github.akmal2409.netflix.videoslicer.job.exception;

import java.util.UUID;

public class DuplicateJobException extends JobExecutionFailureException {

  public DuplicateJobException(String message, UUID jobId) {
    super(message, jobId);
  }
}
