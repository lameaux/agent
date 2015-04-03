package com.euromoby.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;

@Component
public class HostAddrCommand extends CommandBase implements Command {

	public static final String NAME = "hostaddr";
	
	private Config config;
	
	@Autowired
	public HostAddrCommand(Config config) {
		this.config = config;
	}	
	
	@Override
	public String execute(String request) {
		return config.getHost();
	}

	@Override
	public String help() {
		return NAME + "\t\tget configured host address";
	}	
	
	@Override
	public String name() {
		return NAME;
	}

}
