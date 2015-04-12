package com.euromoby.download;

import java.io.File;
import java.util.Map;

import com.euromoby.file.FileProvider;
import com.euromoby.job.Job;
import com.euromoby.job.JobDetail;
import com.euromoby.job.JobState;


public class DownloadJob extends Job {

	public static final String ERROR_PARAMS_EMPTY = "Mandatory parameters are missing";
	public static final String ERROR_PARAM_MISSING = "%s is missing";
	
	public static final String PARAM_URL = "url";
	public static final String PARAM_LOCATION = "location";
	public static final String PARAM_NOPROXY = "noproxy";
	
	private DownloadClient downloadClient;
	private FileProvider fileProvider;
	
	public DownloadJob(JobDetail jobDetail, DownloadClient downloadClient, FileProvider fileProvider) {
		super(jobDetail);
		this.downloadClient = downloadClient;
		this.fileProvider = fileProvider;
	}

	@Override
	public JobDetail call() {
		jobDetail.setState(JobState.RUNNING);
		jobDetail.setStartTime(System.currentTimeMillis());
		try {
			Map<String, String> parameters = jobDetail.getParameters();
			validate(parameters);
			String url = parameters.get(PARAM_URL);
			String location = parameters.get(PARAM_LOCATION);
			boolean noProxy = Boolean.valueOf(parameters.get(PARAM_NOPROXY));			

			File targetFile = fileProvider.getTargetFile(location);
			downloadClient.download(url, targetFile, noProxy);
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
		if (!parameters.containsKey(PARAM_LOCATION)) {
			throw new IllegalArgumentException(String.format(ERROR_PARAM_MISSING, PARAM_LOCATION));
		}		
	}
	
}
