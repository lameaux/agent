package ping;

import java.util.concurrent.Callable;

import model.AgentId;
import model.PingInfo;
import rest.RestServer;

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
