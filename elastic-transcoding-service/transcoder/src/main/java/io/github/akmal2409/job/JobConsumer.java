package io.github.akmal2409.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.github.akmal2409.job.TranscodingJobManifest.VideoQuality;
import io.github.akmal2409.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.utils.StringUtils;

public class JobConsumer extends DefaultConsumer {

  private static final Logger log = LoggerFactory.getLogger(JobConsumer.class);

  private final ObjectMapper mapper;
  private final S3Store s3Store;
  private final Transcoder transcoder;

  public JobConsumer(Channel channel, ObjectMapper mapper, S3Store s3Store, Transcoder transcoder) {
    super(channel);
    this.mapper = mapper;
    this.s3Store = s3Store;
    this.transcoder = transcoder;
  }

  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
      byte[] body) throws IOException {

    final TranscodingJobManifest manifest = mapper.readValue(body, TranscodingJobManifest.class);

    try {
      validateManifest(manifest);
      log.debug(
          "message=Validated manifest. Starting with the job;jobId={};consumerTag={};jobType=transcoding_job",
          manifest.jobId(), consumerTag);
    } catch (InvalidManifestException e) {
      // no sense to retry if manifest is corrupted
      // TODO: Send it to another queue for logging and auditing and alerting.
      log.error(
          "message=Rejected job because manifest is invalid;jobId={};consumerTag={}j;obType=transcoding_job",
          manifest.jobId(), consumerTag);
      getChannel().basicAck(envelope.getDeliveryTag(), false);
      throw e;
    }

    final String[] segmentNames = manifest.segmentFileNames().clone();
    Arrays.sort(segmentNames);

    // given source key points to the whole "folder" containing index file, audio and segments, we are only interested in segments
    // that is why the path is concatenated with /segments
    final Path segmentsPath = s3Store.downloadSamePrefixFiles(
        manifest.sourceBucket(), manifest.sourceKeyPrefix().concat("/segments"),
        manifest.segmentFileNames(), manifest.jobId().toString()
    );
    log.debug(
        "message=Downloaded segment files for the job. Number of segments {}. Downloaded to: {};jobId={};consumerTag={}j;obType=transcoding_job",
        segmentNames.length, segmentsPath, manifest.jobId(), consumerTag);

    try {
      // so that video is not passed through encoding without any reason (loss of quality)
      if (manifest.skipFirstFrames() > 0) {
        final var firstSegment = segmentsPath.resolve(segmentNames[0]);
        skipVideoFrames(firstSegment, manifest.skipFirstFrames());
        log.debug(
            "message=Skipped {} frames from first segment {};jobId={};consumerTag={};jobType=transcoding_job",
            manifest.skipFirstFrames(), segmentNames[0], manifest.jobId(), consumerTag);
      }

      // each segment is under the segments folder (the one we downloaded to) so we just need to concat those together
      final Path[] segmentPaths = buildSegmentPaths(segmentsPath, segmentNames);
      // because we are transcoding using overlapping segments to get target duration, we need to join them into 1 file with a fixed GOP
      final Path joinedEncodedSegments = segmentsPath.resolve("joined.mp4");

      // so it joins the k-segments into 1 and then cuts after GOP size and gets the first chunk, the rest is discarded as it was only used to get the target segment
      transcoder.joinVideosWithGopSize(segmentPaths, joinedEncodedSegments, manifest.gopSize());

      final Path processedFilesPath = segmentsPath.resolve("processed");

      // the segment we have currently encoded was encoded using highest settings to preserve the quality, that is why we keep it in a separate folder
      // with original bit rate and resolution
      final Path originalQualitySegmentPath = processedFilesPath.resolve(
          String.format("%dx%d-%d", manifest.originalQuality().width(),
              manifest.originalQuality().height(),
              manifest.originalQuality().bitRate()));

      final Path processedSegment = originalQualitySegmentPath
                                        .resolve(SegmentConstants.segmentIndexToFileName(
                                            manifest.segmentIndex()));
      
      transcoder.extractFirstSegment(joinedEncodedSegments, processedSegment,
          manifest.targetSegmentDurationSeconds());
      log.debug(
          "message=Finished transcoding segment with index {} at {};jobId={};consumerTag={};jobType=transcoding_job",
          manifest.segmentIndex(), processedSegment, manifest.jobId(), consumerTag);

      transcodeToDifferentBitRates(manifest, processedSegment, processedFilesPath);

      log.debug(
          "message=Finished transcoding original segment with index {} to {} qualities;jobId={};consumerTag={};jobType=transcoding_job",
          manifest.segmentIndex(), manifest.outputQualities().length, manifest.jobId(),
          consumerTag);

      s3Store.uploadProcessedFiles(manifest.outputBucket(), manifest.outputKeyPrefix(),
          processedFilesPath);
      log.debug(
          "message=Finished uploading segments to bucket;bucket={};key={};jobId={};consumerTag={};jobType=transcoding_job",
          manifest.outputBucket(), manifest.outputKeyPrefix(), manifest.jobId(), consumerTag);

      getChannel().basicAck(envelope.getDeliveryTag(), false);
    } finally {
      FileUtils.deleteDirectory(segmentsPath);
    }
  }

  /**
   * According to void qualities specified in the manifest, it constructs the transcoding jobs to different bit rates and qualities
   * for the {@link Transcoder} class. The output files are stored relative to the <code>contextPath</code> parameter following the naming convention:
   * video_quality_i => $context_path/$widthx$height-$bitRate/segment-i.container
   *
   * @param manifest transcoding job manifest
   * @param segmentPath path to the raw segment that need to be transcoded into multiple qualities and bit rates
   * @param contextPath output directory where the segments are going to be placed.
   */
  private void transcodeToDifferentBitRates(TranscodingJobManifest manifest, Path segmentPath,
      Path contextPath) {
    final VideoQualityTranscodingTask[] tasks = new VideoQualityTranscodingTask[manifest.outputQualities().length];

    VideoQuality quality;
    Path transcodedSegmentPath;

    for (int i = 0; i < manifest.outputQualities().length; i++) {
      quality = manifest.outputQualities()[i];

      transcodedSegmentPath = contextPath.resolve(String.format("%dx%d-%d", quality.width(),
          quality.height(),
          quality.bitRate())).resolve(segmentPath.getFileName().toString());

      try {
        Files.createDirectories(transcodedSegmentPath.getParent());
      } catch (IOException e) {
        throw new TranscodingException(
            "Cannot create a directory for segment quality " + transcodedSegmentPath);
      }

      tasks[i] = new VideoQualityTranscodingTask(transcodedSegmentPath, quality.width(),
          quality.height(), quality.bitRate());
    }

    transcoder.transcodeToMultipleQualities(segmentPath, tasks);
  }

  private Path[] buildSegmentPaths(Path rootPath, String[] segmentNames) {
    final Path[] paths = new Path[segmentNames.length];

    for (int i = 0; i < paths.length; i++) {
      paths[i] = rootPath.resolve(segmentNames[i]);
    }

    return paths;
  }

  /**
   * Uses Transcode class to skip n-frames writing it into a new file and then deleting the preivous
   * one and renaming the new one into the old name.
   *
   * @param videoPath      path to the video whose frames need to be dropped
   * @param skipFrameCount number of first frames that need to be dropped
   */
  private void skipVideoFrames(Path videoPath, int skipFrameCount) throws IOException {
    final var tmpFile = videoPath.getParent().resolve("tmp-skipped.mp4");
    transcoder.skipKFrames(videoPath, tmpFile, skipFrameCount);

    Files.delete(videoPath);
    Files.move(tmpFile,
        videoPath); // rename video with skipped frames into the original input video
  }

  private void validateManifest(TranscodingJobManifest manifest) {
    if (manifest.jobId() == null) {
      throw new InvalidManifestException("jobId", null, "empty");
    }

    if (StringUtils.isEmpty(manifest.sourceBucket())) {
      throw new InvalidManifestException("sourceBucket", manifest.sourceBucket(), "empty");
    }

    if (StringUtils.isEmpty(manifest.sourceKeyPrefix())) {
      throw new InvalidManifestException("sourceKeyPrefix", manifest.sourceKeyPrefix(), "empty");
    }

    if (StringUtils.isEmpty(manifest.outputBucket())) {
      throw new InvalidManifestException("outputBucket", manifest.outputBucket(), "empty");
    }

    if (StringUtils.isEmpty(manifest.outputKeyPrefix())) {
      throw new InvalidManifestException("outputKeyPrefix", manifest.outputKeyPrefix(), "empty");
    }

    if (manifest.segmentFileNames() == null || manifest.segmentFileNames().length == 0) {
      throw new InvalidManifestException("segmentFileNames", manifest.segmentFileNames(),
          "no segments");
    }

    if (manifest.skipFirstFrames() < 0) {
      throw new InvalidManifestException("skipFirstFrames", manifest.skipFirstFrames(),
          "negative skip frame count");
    }

    if (manifest.segmentIndex() < 0) {
      throw new InvalidManifestException("segmentIndex", manifest.skipFirstFrames(),
          "negative value");
    }

    if (manifest.targetSegmentDurationSeconds() < 1) {
      throw new InvalidManifestException("targetSegmentDurationSeconds",
          manifest.targetSegmentDurationSeconds(),
          "duration cannot be smaller than 1 second");
    }

    if (manifest.gopSize() < 1) {
      throw new InvalidManifestException("gopSize", manifest.gopSize(), "GOP smaller than 1");
    }

    if (manifest.outputQualities() == null || manifest.outputQualities().length == 0) {
      throw new InvalidManifestException("outputQualities", null, "no qualities provided");
    }
  }
}
