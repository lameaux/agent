package com.euromoby.ping;

import java.util.concurrent.Callable;

import com.euromoby.model.AgentId;
import com.euromoby.model.PingInfo;
import com.euromoby.rest.RestServer;


public class PingWorker implements Callable<PingInfo> {

	protected AgentId agentId;

	public PingWorker(AgentId agentId) {
		this.agentId = agentId;
	}

	@Override
	public PingInfo call() throws Exception {
		PingSender pingSender = new PingSender();
		PingInfo pingInfo = pingSender.ping(agentId.getHost(), agentId.getBasePort() + RestServer.REST_PORT, false);
		pingInfo.setAgentId(agentId);
		return pingInfo;
	}	
	
}
