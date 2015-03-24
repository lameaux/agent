package job;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import service.Service;
import service.ServiceState;
import storage.job.Job;
import storage.job.JobQueue;
import agent.Agent;
import agent.Configuration;

public class JobScheduler implements Service {
	
	public static final String SERVICE_NAME = "job";
	
	private static final int SLEEP_TIME = 1000;
	
	private static final Logger LOG = LoggerFactory.getLogger(JobScheduler.class); 	

	private volatile ServiceState serviceState = ServiceState.STOPPED;
	private volatile boolean interrupted = false;

	private JobQueue jobQueue;
	
	private Configuration config;	
	private ExecutorService executor;
	
	public JobScheduler(JobQueue jobQueue) {
		this.jobQueue = jobQueue;
		config = Agent.get().getConfig();
		executor = Executors.newFixedThreadPool(config.getJobPoolSize());
	}	

	@Override
	public void run() {

		interrupted = false;
		LOG.info("JobService started");	
		
		while (!interrupted) {
			
			// check 
			while(jobQueue.hasNextJob()) {
				Job job = jobQueue.pollJob();
				executor.submit(job);
			}
			
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException ie) {
				interrupted = true;
				Thread.currentThread().interrupt();
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
