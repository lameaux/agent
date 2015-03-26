package com.euromoby.processor;

import com.euromoby.model.PingInfo;
import com.euromoby.ping.PingSender;
import com.euromoby.storage.ping.PingStatus;
import com.euromoby.utils.StringUtils;


public class PingCommand extends CommandBase implements Command {

	private static final String NO_PROXY = "noproxy"; 
	
	@Override
	public String execute(String request) {
		String[] params = parameters(request);
		if (params.length < 2 || StringUtils.nullOrEmpty(params[0]) || StringUtils.nullOrEmpty(params[1])) {
			return syntaxError();
		}

		String host = params[0];
		String restPort = params[1];
		boolean noProxy = (params.length == 3 && NO_PROXY.equals(params[2]));

		PingStatus pingStatus = new PingStatus();
		long start = System.currentTimeMillis();
		try {
			PingSender pingSender = new PingSender();
			PingInfo pingInfo = pingSender.ping(host, Integer.parseInt(restPort), noProxy);
			return pingInfo.getAgentId().toString() + StringUtils.CRLF + pingStatus.toString() + StringUtils.CRLF +"Response time:" + (pingStatus.getTime() - start);
		} catch (Exception e) {
			pingStatus.setError(true);
			pingStatus.setMessage(e.getMessage());
		} finally {
			pingStatus.setTime(System.currentTimeMillis());
		}
		
		return host+":"+ restPort + StringUtils.CRLF + pingStatus.toString() + StringUtils.CRLF + "Response time:" + (pingStatus.getTime() - start);
		
	}

	@Override
	public String help() {
		return "ping\t<agent-host> <agent-rest-port> [noproxy]\tsend ping to <agent-host>:<agent-rest-port> using proxy" + StringUtils.CRLF
				+ StringUtils.CRLF + 
				"Examples:" + StringUtils.CRLF + 
				"ping\tagent1 21080\t\tuse proxy if available" + StringUtils.CRLF + 
				"ping\tagent1 21080 noproxy\tignore proxy configuration";

	}	

	@Override
	public String name() {
		return "ping";
	}

}
