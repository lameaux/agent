package com.euromoby.job;

import java.util.concurrent.Callable;

public abstract class Job implements Callable<JobDetail> {

	protected JobDetail jobDetail;

	public Job(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}
}
