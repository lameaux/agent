package com.euromoby.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.euromoby.model.AgentId;
import com.euromoby.model.PingInfo;

@Component
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

	public synchronized List<AgentId> getAllForPing() {
		List<AgentId> agentList = new ArrayList<AgentId>();
		for (Map.Entry<AgentId, AgentStatus> entry : agents.entrySet()) {
			AgentId agentId = entry.getKey();
			AgentStatus agentStatus = entry.getValue();
			if (agentStatus.isPingRequired()) {
				agentList.add(agentId);
			}
		}
		return agentList;
	}

	public synchronized void notifyPingReceive(PingInfo pingInfo) {
		if (pingInfo != null) {
			AgentStatus status = getAgentStatus(pingInfo.getAgentId());
			if (status != null) {
				status.setLastPingReceived(System.currentTimeMillis());
			}
		}
	}	

	public synchronized void notifyPingSendSuccess(PingInfo pingInfo) {
		if (pingInfo != null) {
			AgentStatus status = getAgentStatus(pingInfo.getAgentId());
			if (status != null) {
				status.setLastPingSendSuccess(System.currentTimeMillis());
			}
		}
	}	
	
	public synchronized void notifyPingSendAttempt(AgentId agentId) {
		AgentStatus status = getAgentStatus(agentId);
		if (status != null) {
			status.setLastPingSendAttempt(System.currentTimeMillis());
		}
	}

}
