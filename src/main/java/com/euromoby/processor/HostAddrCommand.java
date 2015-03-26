package com.euromoby.processor;

import org.springframework.stereotype.Component;

import com.euromoby.utils.NetUtils;
import com.euromoby.utils.StringUtils;

@Component
public class HostAddrCommand extends CommandBase implements Command {

	@Override
	public String execute(String request) {
		return NetUtils.getHostname();
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
