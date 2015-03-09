package processor;

import agent.Agent;

public class VersionCommand extends CommandBase implements Command {

	public String execute(String request) {
		return Agent.VERSION;
	}

	@Override
	public String name() {
		return "version";
	}

}
