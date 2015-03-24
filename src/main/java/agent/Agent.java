package agent;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import job.JobManager;
import job.JobScheduler;
import model.AgentId;
import ping.PingSender;
import processor.CommandProcessor;
import rest.RestServer;
import service.ServiceManager;
import storage.ping.PingStatusStorage;
import telnet.TelnetServer;
import utils.NetUtils;

public class Agent {

	public static final String TITLE = "Agent";
	public static final String VERSION = "0.1";
	private static final Agent instance = new Agent();

	private Configuration config = new Configuration();
	private ServiceManager serviceManager;
	private PingSender pingSender;
	private PingStatusStorage pingStatusStorage;
	private JobManager jobManager;
	

	private CommandProcessor commandProcessor;

	public void init(Configuration config) {
		this.config = config;
		
		
		
		initServices();
	}

	private void initServices() {



		// storages
		pingStatusStorage = new PingStatusStorage();
		pingSender = new PingSender();
		// managers
		jobManager = new JobManager();
		serviceManager = new ServiceManager();

		// CommandProcessor should be the last
		commandProcessor = new CommandProcessor();

		TelnetServer telnetServer = new TelnetServer();
		serviceManager.registerService(telnetServer);

		RestServer restServer = new RestServer();
		serviceManager.registerService(restServer);
		
		// job scheduler

		JobScheduler jobScheduler = new JobScheduler();
		serviceManager.registerService(jobScheduler);

		for (String serviceName : config.getAutorunServices()) {
			serviceManager.executeAction(serviceName, ServiceManager.ACTION_START);
		}
	}

	public static Agent get() {
		return instance;
	}

	public AgentId getAgentId() {
		return new AgentId(NetUtils.getHostname(), config.getBasePort());
	}

	public Configuration getConfig() {
		return config;
	}

	public PingSender getPingSender() {
		return pingSender;
	}

	public PingStatusStorage getPingStatusStorage() {
		return pingStatusStorage;
	}
	
	public ServiceManager getServiceManager() {
		return serviceManager;
	}

	public CommandProcessor getCommandProcessor() {
		return commandProcessor;
	}

	public JobManager getJobManager() {
		return jobManager;
	}
	
	public static void main(String[] args) {
		// setup netty logger
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());		
		// initialize agent
		instance.init(new Configuration());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				instance.getServiceManager().shutdownAll();
			}
		});

		// prevent from close
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
		}

	}

}
