package com.euromoby.model;

public class PingInfo {
	private AgentId agentId;
	private boolean hasNewJobs = false;
	private long freeSpace;

	public PingInfo(AgentId agentId) {
		this.agentId = agentId;
	}

	public AgentId getAgentId() {
		return agentId;
	}

	public void setAgentId(AgentId agentId) {
		this.agentId = agentId;
	}

	public boolean isHasNewJobs() {
		return hasNewJobs;
	}

	public void setHasNewJobs(boolean hasNewJobs) {
		this.hasNewJobs = hasNewJobs;
	}

	public long getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(long freeSpace) {
		this.freeSpace = freeSpace;
	}

}
