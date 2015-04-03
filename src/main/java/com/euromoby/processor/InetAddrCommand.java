package com.euromoby.processor;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import com.euromoby.utils.NetUtils;
import com.euromoby.utils.StringUtils;

@Component
public class InetAddrCommand extends CommandBase implements Command {

	public static final String NAME = "inetaddr";
	
	@Override
	public String execute(String request) {
		StringBuffer sb = new StringBuffer();
		sb.append(NetUtils.getHostname()).append(StringUtils.CRLF);
		sb.append(Arrays.toString(NetUtils.getAllInterfaces().toArray()));
		return sb.toString();
	}

	@Override
	public String help() {
		return NAME + "\t\tshow machine hostname and available network addresses";
	}	
	
	@Override
	public String name() {
		return NAME;
	}

}
