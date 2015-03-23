package agent;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
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
	// CommandProcessor should be the last
	private CommandProcessor commandProcessor;

	public void init(Configuration config) {
		this.config = config;
		initServices();
	}

	private void initServices() {

		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		serviceManager = new ServiceManager();
		pingStatusStorage = new PingStatusStorage();
		pingSender = new PingSender();
		// CommandProcessor should be the last
		commandProcessor = new CommandProcessor();

		TelnetServer telnet = new TelnetServer();
		serviceManager.registerService(telnet);

		RestServer rest = new RestServer();
		serviceManager.registerService(rest);

		for (String serviceName : config.getAutorunServices()) {
			serviceManager.executeAction(serviceName, ServiceManager.ACTION_START);
		}
	}

	public static Agent get() {
		return instance;
	}

	public AgentId getAgentId() {
		return new AgentId(NetUtils.getHostname(), Agent.VERSION, config.getBasePort());
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

	public static void main(String[] args) {
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
