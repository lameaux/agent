package agent;

import java.io.IOException;
import java.util.Properties;

public class Configuration {

	private Properties properties = new Properties();
	
	public Configuration() {
		loadDefaultProperties();
	}
	
	private void loadDefaultProperties() {
		try {
			properties.load(Configuration.class.getClassLoader().getResourceAsStream("default.properties"));
		} catch (IOException e) {
			// ignore
		}		
	}

	public Properties getProperties() {
		return properties;
	}
	
	public static final String TELNET_PORT = "agent.telnet.port";
	public static final String DEFAULT_TELNET_PORT = "21023";

	public static final String REST_PORT = "agent.rest.port";	
	public static final String DEFAULT_REST_PORT = "21080";

	public static final String AUTORUN = "agent.autorun";
	public static final String DEFAULT_AUTORUN = "telnet";	
	
	public int getTelnetPort() {
		return Integer.parseInt(properties.getProperty(TELNET_PORT, DEFAULT_TELNET_PORT));
	}

	public int getRestPort() {
		return Integer.parseInt(properties.getProperty(REST_PORT, DEFAULT_REST_PORT));
	}

	public String get(String key) {
		return properties.getProperty(key);
	}
	
	public String[] getAutorunServices() {
		String autorun = properties.getProperty(AUTORUN, DEFAULT_AUTORUN).trim();
		return autorun.split(",");
	}	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Object k : properties.keySet()) {
			sb.append(k).append("=").append(properties.get(k)).append("\r\n");
		}
		return sb.toString();
	}
	
}

