package com.euromoby.video;

import java.io.File;
import java.util.Map;

import com.euromoby.download.DownloadClient;
import com.euromoby.ffmpeg.Ffmpeg;
import com.euromoby.ffmpeg.FfmpegFormat;
import com.euromoby.file.FileProvider;
import com.euromoby.job.Job;
import com.euromoby.job.JobDetail;
import com.euromoby.job.JobState;

public class GrabVideoJob extends Job {

	public static final String ERROR_PARAMS_EMPTY = "Mandatory parameters are missing";
	public static final String ERROR_PARAM_MISSING = "%s is missing";

	public static final String PARAM_URL = "url";
	public static final String PARAM_VIDEO_LOCATION = "video_location";
	public static final String PARAM_THUMB_LOCATION_PATTERN = "thumb_location_pattern";
	public static final String PARAM_THUMB_COUNT = "thumb_count";
	public static final String PARAM_THUMB_WIDTH = "thumb_width";
	public static final int PARAM_THUMB_WIDTH_DEFAULT = -1;
	public static final String PARAM_THUMB_HEIGHT = "thumb_height";
	public static final int PARAM_THUMB_HEIGHT_DEFAULT = -1;
	
	public static final String PARAM_NOPROXY = "noproxy";

	private DownloadClient downloadClient;
	private FileProvider fileProvider;
	private Ffmpeg ffmpeg;

	public GrabVideoJob(JobDetail jobDetail, DownloadClient downloadClient, FileProvider fileProvider, Ffmpeg ffmpeg) {
		super(jobDetail);
		this.downloadClient = downloadClient;
		this.fileProvider = fileProvider;
		this.ffmpeg = ffmpeg;
	}

	@Override
	public JobDetail call() {
		jobDetail.setState(JobState.RUNNING);
		jobDetail.setStartTime(System.currentTimeMillis());
		try {
			Map<String, String> parameters = jobDetail.getParameters();
			validate(parameters);
			String url = parameters.get(PARAM_URL);
			String videoLocation = parameters.get(PARAM_VIDEO_LOCATION);
			boolean noProxy = Boolean.valueOf(parameters.get(PARAM_NOPROXY));
			
			File videoFile = fileProvider.getTargetFile(videoLocation);			
			downloadClient.download(url, videoFile, noProxy);

			// create thumbnails
			FfmpegFormat ffmpegFormat = ffmpeg.getFormat(videoFile.getCanonicalPath());
			if (ffmpegFormat != null) {
				int duration = (int) Math.round(ffmpegFormat.getDuration());
				int thumbnailCount = Integer.valueOf(parameters.get(PARAM_THUMB_COUNT));
				int chunkLength = duration / (thumbnailCount + 1);
				
				int thumbnailWidth = PARAM_THUMB_WIDTH_DEFAULT;
				if (parameters.containsKey(PARAM_THUMB_WIDTH)) {
					thumbnailWidth = Integer.valueOf(parameters.get(PARAM_THUMB_WIDTH));
				}
				int thumbnailHeight = -1;
				if (parameters.containsKey(PARAM_THUMB_HEIGHT)) {
					thumbnailHeight = Integer.valueOf(parameters.get(PARAM_THUMB_HEIGHT));
				}				
				for (int i=1; i<=thumbnailCount; i++) {
					String outputFileName = String.format(parameters.get(PARAM_THUMB_LOCATION_PATTERN), i);
					File thumbFile = fileProvider.getTargetFile(outputFileName); 
					ffmpeg.createThumbnail(videoFile.getCanonicalPath(), chunkLength*i, thumbnailWidth, thumbnailHeight, thumbFile.getCanonicalPath());
				}
			}
			jobDetail.setState(JobState.FINISHED);
		} catch (Exception e) {
			jobDetail.setError(true);
			jobDetail.setMessage(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			jobDetail.setState(JobState.FAILED);
		} finally {
			jobDetail.setFinishTime(System.currentTimeMillis());
		}
		return jobDetail;
	}

	private void validate(Map<String, String> parameters) throws Exception {
		if (parameters == null || parameters.isEmpty()) {
			throw new IllegalArgumentException(ERROR_PARAMS_EMPTY);
		}
		if (!parameters.containsKey(PARAM_URL)) {
			throw new IllegalArgumentException(String.format(ERROR_PARAM_MISSING, PARAM_URL));
		}
		if (!parameters.containsKey(PARAM_VIDEO_LOCATION)) {
			throw new IllegalArgumentException(String.format(ERROR_PARAM_MISSING, PARAM_VIDEO_LOCATION));
		}
		if (!parameters.containsKey(PARAM_THUMB_LOCATION_PATTERN)) {
			throw new IllegalArgumentException(String.format(ERROR_PARAM_MISSING, PARAM_THUMB_LOCATION_PATTERN));
		}
		if (!parameters.containsKey(PARAM_THUMB_COUNT)) {
			throw new IllegalArgumentException(String.format(ERROR_PARAM_MISSING, PARAM_THUMB_COUNT));
		}
	}

}
