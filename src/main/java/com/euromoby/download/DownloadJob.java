package com.euromoby.download;

import java.util.Map;

import com.euromoby.job.Job;
import com.euromoby.job.JobDetail;
import com.euromoby.job.JobState;


public class DownloadJob extends Job {

	public static final String PARAM_URL = "url";
	public static final String PARAM_LOCATION = "location";
	public static final String PARAM_NOPROXY = "noproxy";
	
	private DownloadClient downloadClient;
	
	public DownloadJob(JobDetail jobDetail, DownloadClient downloadClient) {
		super(jobDetail);
		this.downloadClient = downloadClient;
	}

	@Override
	public JobDetail call() throws Exception {
		jobDetail.setState(JobState.RUNNING);
		jobDetail.setStartTime(System.currentTimeMillis());
		try {
			Map<String, String> parameters = jobDetail.getParameters();
			validate(parameters);
			String url = parameters.get(PARAM_URL);
			String location = parameters.get(PARAM_LOCATION);
			boolean noProxy = Boolean.valueOf(parameters.get(PARAM_NOPROXY));			

			downloadClient.download(url, location, noProxy);
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
			throw new IllegalArgumentException("Mandatory parameters are missing");
		}	
		if (!parameters.containsKey(PARAM_URL)) {
			throw new IllegalArgumentException(PARAM_URL + " is missing");
		}
		if (!parameters.containsKey(PARAM_LOCATION)) {
			throw new IllegalArgumentException(PARAM_LOCATION + " is missing");
		}		
	}
	
}
