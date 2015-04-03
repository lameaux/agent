package com.euromoby.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.utils.StringUtils;

@Component
public class ConfigCommand extends CommandBase implements Command {

	public static final String NAME = "config";
	
	private Config config;
	
	@Autowired
	public ConfigCommand(Config config) {
		this.config = config;
	}
	
	@Override
	public String execute(String request) {
		
		String[] params = parameters(request);
		if (params.length == 1 && !StringUtils.nullOrEmpty(params[0])) {
			return config.get(params[0]);
		}		
		return StringUtils.printProperties(config.getProperties(), null);
	}

	@Override
	public String help() {
		return "config\t\t\tlist configuration parameters" + StringUtils.CRLF +
				"config\t<parameter>\tshow parameter value";
	}	
	
	@Override
	public String name() {
		return NAME;
	}

}
