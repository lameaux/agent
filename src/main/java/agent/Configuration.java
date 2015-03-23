package agent;

import java.io.IOException;
import java.util.Properties;

import utils.StringUtils;
import utils.SystemUtils;

public class Configuration {

	private Properties properties = new Properties();

	public Configuration() {
		loadDefaultProperties();
	}

	public Configuration(Properties properties) {
		this.properties = properties;
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

	public static final String AGENT_BASE_PORT = "agent.base.port";
	public static final String DEFAULT_AGENT_BASE_PORT = "21000";	
	
	public static final String TELNET_PORT = "agent.telnet.port";
	public static final String DEFAULT_TELNET_PORT = "23";

	public static final String REST_PORT = "agent.rest.port";
	public static final String DEFAULT_REST_PORT = "80";

	public static final String AUTORUN = "agent.autorun";
	public static final String DEFAULT_AUTORUN = "telnet";

	public static final String REST_UPLOAD_PATH = "agent.rest.upload.path";
	public static final String DEFAULT_REST_UPLOAD_PATH = null; // home directory;	

	public static final String DOWNLOAD_PATH = "agent.download.path";
	public static final String DEFAULT_DOWNLOAD_PATH = null; // home directory;	
	
	
	public static final String HTTP_PROXY_HOST = "agent.http.proxy.host";
	public static final String HTTP_PROXY_PORT = "agent.http.proxy.port";	
	
	public int getBasePort() {
		return Integer.parseInt(properties.getProperty(AGENT_BASE_PORT, DEFAULT_AGENT_BASE_PORT));
	}	
	
	public int getTelnetPort() {
		return getBasePort() + Integer.parseInt(properties.getProperty(TELNET_PORT, DEFAULT_TELNET_PORT));
	}

	public int getRestPort() {
		return getBasePort() + Integer.parseInt(properties.getProperty(REST_PORT, DEFAULT_REST_PORT));
	}

	public boolean isRestSsl() {
		return false;
	}
	
	public String getHttpProxyHost() {
		return properties.getProperty(HTTP_PROXY_HOST);
	}
	
	public int getHttpProxyPort() {
		return Integer.parseInt(properties.getProperty(HTTP_PROXY_PORT, "0"));
	}
	
	public boolean isHttpProxy() {
		return !StringUtils.nullOrEmpty(getHttpProxyHost());
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
			uploadPath = SystemUtils.getUserHome();
		}
		return uploadPath;
	}
	
	public String getDownloadPath() {
		String downloadPath = properties.getProperty(DOWNLOAD_PATH, DEFAULT_DOWNLOAD_PATH);
		if (StringUtils.nullOrEmpty(downloadPath)) {
			downloadPath = SystemUtils.getUserHome();
		}
		return downloadPath;		
	}
	
	@Override
	public String toString() {
		return StringUtils.printProperties(properties, null);
	}

}
