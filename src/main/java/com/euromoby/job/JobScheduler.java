package com.euromoby.job;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.job.model.JobDetail;
import com.euromoby.job.model.JobState;
import com.euromoby.service.SchedulerService;

@Component
public class JobScheduler extends SchedulerService {

	public static final String SERVICE_NAME = "job";

	private static final Logger LOG = LoggerFactory.getLogger(JobScheduler.class);

	private JobManager jobManager;
	private JobFactory jobFactory;

	private Config config;
	private ExecutorService executor;
	private ExecutorCompletionService<JobDetail> completionService;

	@Autowired
	public JobScheduler(Config config, JobManager jobManager, JobFactory jobFactory) {
		this.config = config;
		this.jobManager = jobManager;
		this.jobFactory = jobFactory;

		executor = Executors.newFixedThreadPool(this.config.getJobPoolSize());
		completionService = new ExecutorCompletionService<JobDetail>(executor);
	}

	@Override
	public void executeInternal() throws InterruptedException {
		checkCompletedJobs();
		scheduleNextJobs();
	}
	
	protected void checkCompletedJobs() throws InterruptedException {
		// check for completed jobs
		Future<JobDetail> jobStatusFuture = completionService.poll();
		if (jobStatusFuture != null) {
			try {
				jobManager.notify(jobStatusFuture.get());
			} catch (ExecutionException e) {
				LOG.warn("Job terminated with error", e.getCause());
			}
		}
	}

	protected void scheduleNextJobs() {
		// check for new job
		if (jobManager.hasNewJob()) {
			JobDetail jobDetail = jobManager.getNextJob();
			jobManager.notify(jobDetail);
			try {
				Job job = jobFactory.createJob(jobDetail);
				completionService.submit(job);
				jobDetail.setState(JobState.WAITING);
			} catch (Exception e) {
				jobDetail.setError(true);
				jobDetail.setMessage(e.getMessage());
				jobDetail.setState(JobState.FAILED);
			} finally {
				jobManager.notify(jobDetail);
			}
		}
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

}
