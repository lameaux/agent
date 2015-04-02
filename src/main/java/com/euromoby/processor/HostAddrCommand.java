package com.euromoby.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.utils.StringUtils;

@Component
public class HostAddrCommand extends CommandBase implements Command {

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
		return "hostaddr\t\t\tget host address" + StringUtils.CRLF +
				"hostaddr\t<address>\tset host address";
	}	
	
	@Override
	public String name() {
		return "hostaddr";
	}

}
