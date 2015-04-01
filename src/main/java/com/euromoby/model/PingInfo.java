package com.euromoby.model;

public class PingInfo {
	private AgentId agentId;
	private long jobsTimestamp = 0;
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

	public long getJobsTimestamp() {
		return jobsTimestamp;
	}

	public void setJobsTimestamp(long jobsTimestamp) {
		this.jobsTimestamp = jobsTimestamp;
	}

	public long getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(long freeSpace) {
		this.freeSpace = freeSpace;
	}

}
