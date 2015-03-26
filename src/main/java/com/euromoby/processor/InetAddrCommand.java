package com.euromoby.processor;

import java.util.Arrays;

import com.euromoby.utils.NetUtils;
import com.euromoby.utils.StringUtils;


public class InetAddrCommand extends CommandBase implements Command {

	@Override
	public String execute(String request) {
		StringBuffer sb = new StringBuffer();
		sb.append("hostname: ").append(NetUtils.getHostname()).append(StringUtils.CRLF);
		sb.append("addresses: ").append(Arrays.toString(NetUtils.getAllInterfaces().toArray()));
		return sb.toString();
	}

	@Override
	public String help() {
		return "inetaddr\t\tshow Agent hostname and addresses";
	}	
	
	@Override
	public String name() {
		return "inetaddr";
	}

}
