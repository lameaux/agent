package com.euromoby.model;

import com.euromoby.utils.StringUtils;

public class AgentId {
	private String host;
	private int basePort;

	public AgentId(String host, int basePort) {
		if (StringUtils.nullOrEmpty(host)) {
			throw new IllegalArgumentException("Invalid host " + host);
		}
		this.host = host;
		this.basePort = basePort;
	}

	public AgentId(String agentId) {
		if (StringUtils.nullOrEmpty(agentId)) {
			throw new IllegalArgumentException("Invalid AgentId agentId");
		}
		try {
			String agentIdParts[] = agentId.split(":", 2);
			if (agentIdParts.length != 2) {
				throw new IllegalArgumentException("Invalid AgentId " + agentId);
			}
			this.host = agentIdParts[0];
			if (StringUtils.nullOrEmpty(host)) {
				throw new IllegalArgumentException("Invalid host " + host);
			}			
			this.basePort = Integer.parseInt(agentIdParts[1]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid AgentId " + agentId);
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getBasePort() {
		return basePort;
	}

	public void setBasePort(int basePort) {
		this.basePort = basePort;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + basePort;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentId other = (AgentId) obj;
		if (basePort != other.basePort)
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return host + ":" + basePort;
	}

}
