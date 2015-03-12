package agent;

import service.ServiceManager;
import telnet.TelnetServer;

public class Agent {

	public static final String VERSION = "0.1";
	private static Agent instance = new Agent();

	private Configuration config;
	public static ServiceManager serviceManager = new ServiceManager();

	public void init(Configuration config) {
		this.config = config;
		initServices();
	}

	public static void main(String[] args) {
		instance.init(new Configuration());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				serviceManager.shutdownAll();
			}
		});

	}

	private void initServices() {
		TelnetServer telnet = new TelnetServer(config.getTelnetPort());
		serviceManager.registerService(telnet);
		
		for (String serviceName : config.getAutorunServices()) {
			serviceManager.startService(serviceName);
		}
	}

}
