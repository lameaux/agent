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
import com.euromoby.service.Service;
import com.euromoby.service.ServiceState;

@Component
public class JobScheduler implements Service {

	public static final String SERVICE_NAME = "job";

	protected static final int SLEEP_TIME = 1000;

	private static final Logger LOG = LoggerFactory.getLogger(JobScheduler.class);

	private volatile ServiceState serviceState = ServiceState.STOPPED;
	private volatile boolean interrupted = false;

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

	protected boolean isInterrupted() {
		return interrupted;
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
	public void run() {
		interrupted = false;
		LOG.info("JobScheduler started");

		while (!interrupted) {
			try {
				checkCompletedJobs();
				scheduleNextJobs();
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				interrupted = true;
				break;
			}
		}
		serviceState = ServiceState.STOPPED;
		LOG.info("JobScheduler stopped");
	}

	@Override
	public void startService() {
		serviceState = ServiceState.RUNNING;
		new Thread(this).start();
	}

	@Override
	public void stopService() {
		interrupted = true;
		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public ServiceState getServiceState() {
		return serviceState;
	}

}
