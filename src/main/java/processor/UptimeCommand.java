package processor;

public class UptimeCommand implements Command {

	private long started;

	public UptimeCommand() {
		started = System.currentTimeMillis();
	}

	public String execute(String request) {
		long serverUptimeSeconds = (System.currentTimeMillis() - started) / 1000;

		return String.format("%d days %d hours %d minutes %d seconds", serverUptimeSeconds / 86400,
				(serverUptimeSeconds % 86400) / 3600, ((serverUptimeSeconds % 86400) % 3600) / 60,
				((serverUptimeSeconds % 86400) % 3600) % 60);
	}

	public boolean match(String request) {
		return "uptime".equalsIgnoreCase(request);
	}

}
