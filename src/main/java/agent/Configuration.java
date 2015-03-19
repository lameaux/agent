package agent;

import java.io.IOException;
import java.util.Properties;

import utils.StringUtils;

public class Configuration {

	private Properties properties = new Properties();

	public Configuration() {
		loadDefaultProperties();
	}

	private void loadDefaultProperties() {
		try {
			properties.load(Configuration.class.getResourceAsStream("default.properties"));
		} catch (IOException e) {
			e.printStackTrace();
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

	public static final String REST_UPLOAD_PATH = "agent.rest.upload.path";
	public static final String DEFAULT_REST_UPLOAD_PATH = null; // home directory;	
	
	public int getTelnetPort() {
		return Integer.parseInt(properties.getProperty(TELNET_PORT, DEFAULT_TELNET_PORT));
	}

	public int getRestPort() {
		return Integer.parseInt(properties.getProperty(REST_PORT, DEFAULT_REST_PORT));
	}

	public boolean isRestSsl() {
		return false;
	}
	
	public String get(String key) {
		return properties.getProperty(key);
	}

	public String[] getAutorunServices() {
		String autorun = properties.getProperty(AUTORUN, DEFAULT_AUTORUN).trim();
		return autorun.split(",");
	}

	public String getRestUploadPath() {
		String uploadPath = properties.getProperty(REST_UPLOAD_PATH, DEFAULT_REST_UPLOAD_PATH);
		if (StringUtils.nullOrEmpty(uploadPath)) {
			uploadPath = System.getProperty("user.home");
		}
		return uploadPath;
	}
	
	@Override
	public String toString() {
		return StringUtils.printProperties(properties, null);
	}

}
