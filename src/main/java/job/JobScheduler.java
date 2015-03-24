package job;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import service.Service;
import service.ServiceState;
import utils.DateUtils;
import agent.Agent;
import agent.Configuration;

public class JobScheduler implements Service {
	
	public static final String SERVICE_NAME = "job";
	
	private static final int SLEEP_TIME = 1000;
	
	private static final Logger LOG = LoggerFactory.getLogger(JobScheduler.class); 	

	private volatile ServiceState serviceState = ServiceState.STOPPED;
	private volatile boolean interrupted = false;

	private JobQueue jobQueue;
	private JobStatusNotifier jobStatusNotifier;
	
	private Configuration config;	
	private ExecutorService executor;
	private ExecutorCompletionService<JobStatus> completionService;
	
	public JobScheduler(JobQueue jobQueue, JobStatusNotifier jobStatusNotifier) {
		this.jobQueue = jobQueue;
		this.jobStatusNotifier = jobStatusNotifier;
		
		config = Agent.get().getConfig();
		executor = Executors.newFixedThreadPool(config.getJobPoolSize());
		completionService = new ExecutorCompletionService<JobStatus>(executor);
	}	

	@Override
	public void run() {

		interrupted = false;
		LOG.info("JobService started");	
		
		while (!interrupted) {
			
			// check for completed jobs
			Future<JobStatus> jobStatusFuture = completionService.poll();
			if (jobStatusFuture != null) {
				try {
					jobStatusNotifier.notify(jobStatusFuture.get());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					interrupted = true;
					break;
				} catch (ExecutionException e) {
					LOG.warn("Job terminated with error", e.getCause());
				}
			}
			
			// check for new job
			if (jobQueue.hasNextJob()) {
				Job job = jobQueue.pollJob();
				completionService.submit(job);
				LOG.debug("{} started at {}", job.name(), DateUtils.iso(System.currentTimeMillis()));
			}
			
			// sleep
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				interrupted = true;
				break;
			}
			
		}
		serviceState = ServiceState.STOPPED;
		LOG.info("JobService stopped");		
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
