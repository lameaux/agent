package agent;

import service.ServiceManager;
import telnet.TelnetServer;

public class Agent {

	public static final String VERSION = "0.1";
	private static final Agent instance = new Agent();

	private Configuration config = new Configuration();
	public ServiceManager serviceManager = new ServiceManager();

	public void init(Configuration config) {
		this.config = config;
		initServices();
	}

	private void initServices() {
		TelnetServer telnet = new TelnetServer(config.getTelnetPort());
		serviceManager.registerService(telnet);

		for (String serviceName : config.getAutorunServices()) {
			serviceManager.executeAction(serviceName, ServiceManager.ACTION_START);
		}
	}

	public static Agent get() {
		return instance;
	}

	public Configuration getConfig() {
		return config;
	}
	
	
	public ServiceManager getServiceManager() {
		return serviceManager;
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
