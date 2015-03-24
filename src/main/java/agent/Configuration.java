package agent;

import java.io.File;
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
	public static final String DEFAULT_AUTORUN = "rest,job";

	public static final String AGENT_ROOT_PATH = "agent.root.path";

	public static final String AGENT_APP_PATH = "agent.app.path";
	public static final String DEFAULT_AGENT_APP_PATH = "agent";	
	
	public static final String REST_UPLOAD_PATH = "agent.rest.upload.path";
	public static final String DEFAULT_REST_UPLOAD_PATH = "files";	

	public static final String DOWNLOAD_PATH = "agent.download.path";
	public static final String DEFAULT_DOWNLOAD_PATH = "files";	
	
	
	public static final String HTTP_PROXY_HOST = "agent.http.proxy.host";
	public static final String HTTP_PROXY_PORT = "agent.http.proxy.port";	

	public static final String JOB_POOL_SIZE = "agent.job.pool.size";
	public static final String DEFAULT_JOB_POOL_SIZE = "2";	
	
	
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

	public String getAgentRootPath() {
		String agentRootPath = properties.getProperty(AGENT_ROOT_PATH);
		if (StringUtils.nullOrEmpty(agentRootPath)) {
			agentRootPath = SystemUtils.getUserHome();
		}
		return agentRootPath;
	}
	
	public String getAgentAppPath() {
		String agentAppPath = properties.getProperty(AGENT_APP_PATH, DEFAULT_AGENT_APP_PATH);
		return getAgentRootPath() + File.separatorChar + agentAppPath;
	}
	
	
	public String getRestUploadPath() {
		String uploadPath = properties.getProperty(REST_UPLOAD_PATH, DEFAULT_REST_UPLOAD_PATH);
		return getAgentAppPath() + File.separatorChar + uploadPath;
	}
	
	public String getDownloadPath() {
		String downloadPath = properties.getProperty(DOWNLOAD_PATH, DEFAULT_DOWNLOAD_PATH);
		return getAgentAppPath() + File.separatorChar + downloadPath;		
	}
	
	public int getJobPoolSize() {
		return Integer.parseInt(properties.getProperty(JOB_POOL_SIZE, DEFAULT_JOB_POOL_SIZE));
	}
	
	@Override
	public String toString() {
		return StringUtils.printProperties(properties, null);
	}

}
