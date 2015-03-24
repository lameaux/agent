package download;

import job.Job;
import job.JobDetail;
import job.JobState;

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
	public JobDetail call() throws Exception {
		DownloadClient dc = new DownloadClient();
		JobDetail jobStatus = createJobStatus();
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
	public JobDetail createJobStatus() {
		JobDetail jobStatus = new JobDetail(this.getClass().getCanonicalName(), getUuid(), getScheduleTime(), null);
		return jobStatus;
	}
	
}
