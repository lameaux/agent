package processor;

import utils.StringUtils;
import agent.Agent;
import agent.Configuration;

public class ConfigCommand extends CommandBase implements Command {

	public String execute(String request) {
		Configuration config = Agent.get().getConfig();
		
		String[] params = parameters(request);
		if (params.length == 1 && !StringUtils.nullOrEmpty(params[0])) {
			return config.get(params[0]);
		}		
		return config.toString();
	}

	@Override
	public String name() {
		return "config";
	}

}
