package com.euromoby.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.download.DownloadClient;
import com.euromoby.ffmpeg.Ffmpeg;
import com.euromoby.file.FileProvider;
import com.euromoby.job.model.JobDetail;
import com.euromoby.upload.UploadClient;
import com.euromoby.upload.UploadJob;
import com.euromoby.video.GrabVideoJob;

@Component
public class JobFactory {
	
	private UploadClient uploadClient;
	private DownloadClient downloadClient;
	private GetJobsClient getJobsClient;
	
	private FileProvider fileProvider; 
	private Ffmpeg ffmpeg;
	
	@SuppressWarnings("rawtypes")
	private Class[] jobClasses = new Class[]{
			UploadJob.class, GetNewJobsJob.class, GrabVideoJob.class
	};
	
	@SuppressWarnings("rawtypes")
	public Class[] getJobClasses() {
		return jobClasses;
	}
	
	@Autowired
	public void setUploadClient(UploadClient uploadClient) {
		this.uploadClient = uploadClient;
	}

	@Autowired
	public void setDownloadClient(DownloadClient downloadClient) {
		this.downloadClient = downloadClient;
	}

	@Autowired	
	public void setGetJobsClient(GetJobsClient getJobsClient) {
		this.getJobsClient = getJobsClient;
	}

	@Autowired
	public void setFileProvider(FileProvider fileProvider) {
		this.fileProvider = fileProvider;
	}

	@Autowired
	public void setFfmpeg(Ffmpeg ffmpeg) {
		this.ffmpeg = ffmpeg;
	}	
	
	public Job createJob(JobDetail jobDetail) throws Exception {
		if (GrabVideoJob.class.getCanonicalName().equals(jobDetail.getJobClass())) {
			return new GrabVideoJob(jobDetail, downloadClient, fileProvider, ffmpeg);
		}
		if (UploadJob.class.getCanonicalName().equals(jobDetail.getJobClass())) {
			return new UploadJob(jobDetail, uploadClient);
		}
		if (GetNewJobsJob.class.getCanonicalName().equals(jobDetail.getJobClass())) {
			return new GetNewJobsJob(jobDetail, getJobsClient);
		}
		throw new Exception(jobDetail.getJobClass() + " is not supported");
	}
	
}
