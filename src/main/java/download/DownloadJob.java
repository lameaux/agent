package download;

import job.Job;
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
		dc.download(url, location, noProxy);
		return new JobStatus();
	}

}
