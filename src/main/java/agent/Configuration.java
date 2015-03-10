package agent;

import telnet.TelnetServer;

public class Configuration {

	public static final int DEFAULT_TELNET_PORT = 21023;
	public static final int DEFAULT_WEB_PORT = 21080;

	public int getTelnetPort() {
		return DEFAULT_TELNET_PORT;
	}

	public int getWebPort() {
		return DEFAULT_WEB_PORT;
	}

	public String[] getActiveServices() {
		return new String[] {TelnetServer.SERVICE_NAME};
	}	
	
}
