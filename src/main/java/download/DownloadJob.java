package download;

import job.Job;
import job.JobState;
import job.JobStatus;

public class DownloadJob extends Job {

	private String url;
	private String location;
	private boolean noProxy;

	public DownloadJob(String url, String location, boolean noProxy) {
		this(url, location, noProxy, System.currentTimeMillis());
	}

	public DownloadJob(String url, String location, boolean noProxy, long startTime) {
		super(startTime);
		this.url = url;
		this.location = location;
		this.noProxy = noProxy;
	}

	@Override
	public JobStatus call() throws Exception {
		DownloadClient dc = new DownloadClient();
		JobStatus jobStatus = createJobStatus();
		jobStatus.setStartTime(System.currentTimeMillis());
		try {
			dc.download(url, location, noProxy);
		} catch (Exception e) {
			jobStatus.setError(true);
			jobStatus.setMessage(e.getMessage());
		} finally {
			jobStatus.setFinishTime(System.currentTimeMillis());
			jobStatus.setState(JobState.FINISHED);
		}
		return jobStatus;
	}

	@Override
	public JobStatus createJobStatus() {
		JobStatus jobStatus = new JobStatus(this.getClass().getCanonicalName() , getUuid());
		return jobStatus;
	}
	
}
