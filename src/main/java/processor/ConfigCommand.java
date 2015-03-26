package processor;

import utils.StringUtils;
import agent.Agent;
import agent.Configuration;

public class ConfigCommand extends CommandBase implements Command {

	@Override
	public String execute(String request) {
		Configuration config = Agent.get().getConfig();
		
		String[] params = parameters(request);
		if (params.length == 1 && !StringUtils.nullOrEmpty(params[0])) {
			return config.get(params[0]);
		}		
		return config.toString();
	}

	@Override
	public String help() {
		return "config\t\t\tlist configuration parameters" + StringUtils.CRLF +
				"config\t<parameter>\tshow parameter value";
	}	
	
	@Override
	public String name() {
		return "config";
	}

}
