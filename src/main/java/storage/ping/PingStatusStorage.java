package storage.ping;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import model.AgentId;
import storage.Storage;

public class PingStatusStorage implements Storage<PingStatus> {

	private Map<AgentId, PingStatus> pingStatuses = new ConcurrentHashMap<AgentId, PingStatus>();

	public void setPingStatus(AgentId agentId, PingStatus pingStatus) {
		pingStatuses.put(agentId, pingStatus);
	}

	public PingStatus getPingStatus(AgentId agentId) {
		return pingStatuses.get(agentId);
	}

	public Map<AgentId, PingStatus> getAllPingStatuses() {
		return new HashMap<AgentId, PingStatus>(pingStatuses);
	}
	
}
