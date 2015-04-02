package com.euromoby.agent;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.euromoby.model.AgentId;
import com.euromoby.rest.RestServer;
import com.euromoby.telnet.TelnetServer;
import com.euromoby.utils.NetUtils;
import com.euromoby.utils.StringUtils;
import com.euromoby.utils.SystemUtils;


public class Config {

	private Properties properties = new Properties();

	public Config() {
		loadDefaultProperties();
	}

	public Config(Properties properties) {
		this.properties = properties;
	}
	
	private void loadDefaultProperties() {
		try {
			properties.load(Config.class.getResourceAsStream("default.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final String AGENT_BASE_PORT = "agent.base.port";
	public static final String DEFAULT_AGENT_BASE_PORT = "21000";	
	
	public static final String AGENT_HOST = "agent.host";
	
	public static final String AUTORUN = "agent.autorun";
	public static final String DEFAULT_AUTORUN = "rest,job,ping";

	public static final String AGENT_ROOT_PATH = "agent.root.path";

	public static final String AGENT_APP_PATH = "agent.app.path";
	public static final String DEFAULT_AGENT_APP_PATH = "agent";	
	
	public static final String AGENT_FILES_PATH = "agent.files.path";
	public static final String DEFAULT_AGENT_FILES_PATH = "files";	
	
	public static final String HTTP_PROXY_HOST = "agent.http.proxy.host";
	public static final String HTTP_PROXY_PORT = "agent.http.proxy.port";
	public static final String DEFAULT_HTTP_PROXY_PORT = "3128";

	public static final String JOB_POOL_SIZE = "agent.job.pool.size";
	public static final String DEFAULT_JOB_POOL_SIZE = "4";	
	
	public static final String PING_POOL_SIZE = "agent.ping.pool.size";
	public static final String DEFAULT_PING_POOL_SIZE = "2";	

	public static final String KEYSTORE_PATH = "agent.keystore.path";
	
	public static final String KEYSTORE_STORE_PASSWORD = "agent.keystore.storepass";
	public static final String DEFAULT_KEYSTORE_STORE_PASSWORD = "123456";	

	public static final String KEYSTORE_KEY_PASSWORD = "agent.keystore.keypass";	
	public static final String DEFAULT_KEYSTORE_KEY_PASSWORD = "123456";
	
	
	public String get(String key) {
		return properties.getProperty(key);
	}

	public Properties getProperties() {
		return properties;
	}	
	
	public String getHost() {
		String agentHost = properties.getProperty(AGENT_HOST);
		if (StringUtils.nullOrEmpty(agentHost)) {
			agentHost = NetUtils.getHostname();
		}
		return agentHost;
	}
	
	public AgentId getAgentId() {
		return new AgentId(getHost(), getBasePort());
	}	

	public String[] getAutorunServices() {
		String autorun = properties.getProperty(AUTORUN, DEFAULT_AUTORUN).trim();
		return autorun.split(",");
	}	
	
	
	public int getBasePort() {
		return Integer.parseInt(properties.getProperty(AGENT_BASE_PORT, DEFAULT_AGENT_BASE_PORT));
	}	
	
	public int getTelnetPort() {
		return getBasePort() + TelnetServer.TELNET_PORT;
	}

	public int getRestPort() {
		return getBasePort() + RestServer.REST_PORT;
	}

	public String getKeystorePath() {
		return properties.getProperty(KEYSTORE_PATH);
	}	

	public String getKeystoreStorePass() {
		return properties.getProperty(KEYSTORE_STORE_PASSWORD, DEFAULT_KEYSTORE_STORE_PASSWORD);
	}	
	
	public String getKeystoreKeyPass() {
		return properties.getProperty(KEYSTORE_KEY_PASSWORD, DEFAULT_KEYSTORE_KEY_PASSWORD);
	}
	
	public String getHttpProxyHost() {
		return properties.getProperty(HTTP_PROXY_HOST);
	}
	
	public int getHttpProxyPort() {
		return Integer.parseInt(properties.getProperty(HTTP_PROXY_PORT, DEFAULT_HTTP_PROXY_PORT));
	}
	
	public boolean isHttpProxy() {
		return !StringUtils.nullOrEmpty(getHttpProxyHost());
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
	
	
	public String getAgentFilesPath() {
		String agentFilesPath = properties.getProperty(AGENT_FILES_PATH, DEFAULT_AGENT_FILES_PATH);
		return getAgentAppPath() + File.separatorChar + agentFilesPath;		
	}
	
	public int getJobPoolSize() {
		return Integer.parseInt(properties.getProperty(JOB_POOL_SIZE, DEFAULT_JOB_POOL_SIZE));
	}

	public int getPingPoolSize() {
		return Integer.parseInt(properties.getProperty(PING_POOL_SIZE, DEFAULT_PING_POOL_SIZE));
	}	
	
	@Override
	public String toString() {
		return StringUtils.printProperties(properties, null);
	}

}
