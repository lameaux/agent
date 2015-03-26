package agent;

import java.util.HashMap;
import java.util.Map;

import model.AgentId;

public class AgentManager {

	private Map<AgentId, AgentStatus> agents = new HashMap<AgentId, AgentStatus>();
	
	public synchronized void addAgent(AgentId agentId) {
		if (!agents.containsKey(agentId)) {
			agents.put(agentId, new AgentStatus());
		}
	}
	
	public synchronized AgentStatus getAgentStatus(AgentId agentId) {
		return agents.get(agentId);
	}
}
