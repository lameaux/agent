package com.euromoby.processor;

import org.springframework.stereotype.Component;

@Component
public class UptimeCommand extends CommandBase implements Command {

	public static final String NAME = "uptime";

	private static long started = System.currentTimeMillis();

	@Override
	public String execute(String request) {
		long serverUptimeSeconds = (System.currentTimeMillis() - started) / 1000;

		return String.format("%d days %d hours %d minutes %d seconds", serverUptimeSeconds / 86400, (serverUptimeSeconds % 86400) / 3600,
				((serverUptimeSeconds % 86400) % 3600) / 60, ((serverUptimeSeconds % 86400) % 3600) % 60);
	}

	@Override
	public String help() {
		return NAME + "\t\tshow Agent uptime";
	}

	@Override
	public String name() {
		return NAME;
	}

}
