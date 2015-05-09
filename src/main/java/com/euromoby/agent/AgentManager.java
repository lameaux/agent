package com.euromoby.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.model.AgentId;
import com.euromoby.model.PingInfo;

@Component
public class AgentManager implements InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(AgentManager.class);	
	
	private Map<AgentId, AgentStatus> agents = new HashMap<AgentId, AgentStatus>();
	private Config config;
	
	@Autowired
	public AgentManager(Config config) {
		this.config = config;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		AgentId myAgentId = config.getAgentId();
		String[] agentFriendIds = config.getAgentFriends();
		for (String agentFriendId : agentFriendIds) {
			try {
				AgentId agentId = new AgentId(agentFriendId);
				if (!agentId.equals(myAgentId)) {
					addAgent(agentId);
				}
			} catch (Exception e) {
				LOG.warn("Invalid AgentId '{}'", agentFriendId);
			}
		}
	}	
	
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
				status.setFreeSpace(pingInfo.getFreeSpace());
			}
		}
	}	

	public synchronized void notifyPingSendSuccess(PingInfo pingInfo) {
		if (pingInfo != null) {
			AgentStatus status = getAgentStatus(pingInfo.getAgentId());
			if (status != null) {
				status.setLastPingSendSuccess(System.currentTimeMillis());
				status.setFreeSpace(pingInfo.getFreeSpace());
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
