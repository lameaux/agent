package rest.handler.ping;

public class PingResponse {
	private String hostname;
	private String version;

	public PingResponse(String hostname, String version) {
		super();
		this.hostname = hostname;
		this.version = version;
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

}
