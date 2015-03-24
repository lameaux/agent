package job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import service.Service;
import service.ServiceState;
import agent.Agent;
import agent.Configuration;

public class JobService implements Service {
	
	public static final String SERVICE_NAME = "job";
	
	private static final int SLEEP_TIME = 1000;
	
	private static final Logger LOG = LoggerFactory.getLogger(JobService.class); 	

	private volatile ServiceState serviceState = ServiceState.STOPPED;
	private volatile boolean interrupted = false;
	
	private Configuration config;	
	
	public JobService() {
		this.config = Agent.get().getConfig();
	}	

	@Override
	public void run() {

		serviceState = ServiceState.RUNNING;
		interrupted = false;
		LOG.info("JobService started");	
		
		while (!interrupted) {
			
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
		new Thread(this).start();
	}

	@Override
	public void stopService() {
		interrupted = true;
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
