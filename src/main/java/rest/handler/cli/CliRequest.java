package rest.handler.cli;

public class CliRequest {
	private String request;

	public void setRequest(String request) {
		this.request = request;
	}

	public CliRequest(String request) {
		this.request = request;
	}

	public String getRequest() {
		return request;
	}

}
