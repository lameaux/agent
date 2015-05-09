package com.euromoby.ping;

import java.util.concurrent.Callable;

import com.euromoby.model.AgentId;
import com.euromoby.ping.model.PingInfo;
import com.euromoby.rest.RestServer;


public class PingWorker implements Callable<PingInfo> {

	private AgentId agentId;
	private PingSender pingSender;

	public PingWorker(AgentId agentId, PingSender pingSender) {
		this.agentId = agentId;
		this.pingSender = pingSender;
	}

	@Override
	public PingInfo call() throws Exception {
		PingInfo pingInfo = pingSender.ping(agentId.getHost(), agentId.getBasePort() + RestServer.REST_PORT, false);
		pingInfo.setAgentId(agentId);
		return pingInfo;
	}	
	
}
