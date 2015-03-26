package agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	
	public synchronized List<AgentId> getAll() {
		List<AgentId> agentList = new ArrayList<AgentId>();
		for (AgentId agentId : agents.keySet()) {
			agentList.add(agentId);
		}
		return agentList;
	}

	public synchronized List<AgentId> getActive() {
		List<AgentId> agentList = new ArrayList<AgentId>();
		for (Map.Entry<AgentId, AgentStatus> entry : agents.entrySet()) {
			AgentId agentId = entry.getKey();
			AgentStatus agentStatus = entry.getValue();
			if (agentStatus.isActive()) {
				agentList.add(agentId);
			}
		}
		return agentList;
	}	
	
}
