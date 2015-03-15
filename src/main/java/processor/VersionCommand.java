package processor;

import org.restexpress.Request;

import agent.Agent;

public class VersionCommand extends CommandBase implements Command {

	public static final String NAME = "version";

	@Override
	public String execute(String request) {
		return executeInternal();
	}

	@Override
	public String execute(Request request) {
		return executeInternal();
	}

	private String executeInternal() {
		return Agent.TITLE + " " + Agent.VERSION;
	}

	@Override
	public String name() {
		return NAME;
	}

}
