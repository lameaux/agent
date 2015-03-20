package model;

public class AgentId {
	private String hostname;
	private String version;
	private int basePort;

	public AgentId(String hostname, String version, int baseport) {
		super();
		this.hostname = hostname;
		this.version = version;
		this.basePort = baseport;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
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
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AgentId [hostname=" + hostname + ", version=" + version + ", basePort=" + basePort + "]";
	}

}
