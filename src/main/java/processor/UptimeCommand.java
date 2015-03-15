package processor;

import org.restexpress.Request;

public class UptimeCommand extends CommandBase implements Command {

	private static long started = System.currentTimeMillis();

	@Override
	public String execute(String request) {
		return executeInternal();
	}

	@Override
	public String execute(Request request) {
		return executeInternal();
	}

	private String executeInternal() {
		long serverUptimeSeconds = (System.currentTimeMillis() - started) / 1000;

		return String.format("%d days %d hours %d minutes %d seconds", serverUptimeSeconds / 86400, (serverUptimeSeconds % 86400) / 3600,
				((serverUptimeSeconds % 86400) % 3600) / 60, ((serverUptimeSeconds % 86400) % 3600) % 60);
	}

	@Override
	public String name() {
		return "uptime";
	}

}
