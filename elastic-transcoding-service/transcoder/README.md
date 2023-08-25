# Transcoder Worker

This repository is part of the Elastic Transcoding Service (ETS). Specifically, this code repository contains the source of Java-based worker that can transcode both audio and video (via overlapping video segments)

## General Architecture
### Worker Mode
There are 2 worker modes available:
1. `VIDEO_TRANSCODER` - transcodes video segments. Since input segments are not of equal length, the only way to encode is to take overlapping segments, join and trim them.
2. `AUDIO_TRANSCODER` - transcodes single audio file and produces a single transocded audio file with the desired bit rate.

### Storage
The worker pulls and pushes the data solely to S3 compatible large object storage system. All the required bucket keys and file keys are received through a manifest

### Manifests
#### Video segment transcoding manifest

Below you can see the JSON that the worker expects to be sent to the video transcoding queue.

```json
{
  "jobId": "376c3b41-cdcf-45b5-8773-23e91633c853",
  "sourceBucket": "processed",
  "sourceKeyPrefix": "job-376c3b41-cdcf-45b5-8773-23e91633c853",
  "outputBucket": "transcoded",
  "outputKeyPrefix": "job-376c3b41-cdcf-45b5-8773-23e91633c853",
  "segmentIndex": 3,
  "targetSegmentDurationSeconds": 5,
  "segmentFileNames": [
    "segment-00000006.mp4"
  ],
  "skipFirstFrames": 36,
  "gopSize": 120,
  "outputQualities": [
    {
      "width": 1280,
      "height": 720,
      "bitRate": 6000
    },
    {
      "width": 1280,
      "height": 720,
      "bitRate": 5000
    },
    {
      "width": 1280,
      "height": 720,
      "bitRate": 4000
    },
    {
      "width": 640,
      "height": 360,
      "bitRate": 3000
    }
  ],
  "originalQuality": {
    "width": 1280,
    "height": 720,
    "bitRate": 7366
  }
}
```

Breakdown of keys
* `jobId` - id of the job. Must be unique
* `sourceBucket` - source bucket where the video segments are located 
* `sourceKeyPrefix` - common prefix of the source files (the code expects that segments to be at `sourceKeyPrefix` + `/segments`, it's brittle and will be changed)
* `outputBucket` - output bucket name
* `outputKeyPrefix` - common key prefix for the output files (think of folders)
* `segmentIndex` - the actual segment index we are trying to produce
* `targetSegmentDurationSeconds` - desired segment duration
* `segmentFileName` - array of file names under the `segments` folder (key prefix)
* `skipFirstFrames` - number of frames to skip from the first segment (it is possible that the first segment's part is part of another segment, that is why we need to trim that)
* `gopSize` - desired group of pictures length. It will have exactly gopSize but might have I-Frames in between shall the encoder deem that necessary
* `outputQualities` - desired output qualities for ABR streaming. The output files will be structures as `outputKeyPrefix` + `/$widthx$height-$bitRate/segment-$number.mp4`
* `originalQuality` - original video quality

#### Audio transcoding manifest

Below you can find audio transcoding job manifest that the worker expects to receive from a rabbitMQ queue.

```json
{
  "jobId": "376c3b41-cdcf-45b5-8773-23e91633c853",
  "sourceBucket": "processed",
  "sourceKeyPrefix": "job-376c3b41-cdcf-45b5-8773-23e91633c853",
  "fileName": "audio.m4a",
  "targetBitRate": 320000,
  "codec": "aac",
  "outputBucket": "transcoded",
  "outputKeyPrefix": "job-376c3b41-cdcf-45b5-8773-23e91633c853"
}
```

And here is a breakdown of properties
* `jobId` - ID of a job, must be unique
* `sourceBucket` - source bucket where the audio is
* `sourceKeyPrefix` - key prefix of a source audio file (excluding the file name)
* `fileName` - audio file name
* `targetBitRate` - target bit rate of the audio file
* `codec` - codec (any codec that is supported by ffmpeg)
* `outputBucket` - output bucket name where to store the files
* `outputKeyPrefix` - the common key prefix for the output file(s)
