package com.euromoby.agent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.cdn.CdnServer;
import com.euromoby.model.AgentId;
import com.euromoby.rest.RestServer;
import com.euromoby.telnet.TelnetServer;
import com.euromoby.utils.NetUtils;
import com.euromoby.utils.StringUtils;
import com.euromoby.utils.SystemUtils;

public class Config {

	public static final String PARAM_AGENT_CONFIG = "agent.config";
	public static final String AGENT_PROPERTIES = "agent.properties";
	private static final Logger LOG = LoggerFactory.getLogger(Config.class);

	private Properties properties = new Properties();

	public Config() {

		File agentConfigFile = new File(getJarLocation(), AGENT_PROPERTIES);

		String agentConfigLocation = System.getProperty(PARAM_AGENT_CONFIG);
		if (!StringUtils.nullOrEmpty(agentConfigLocation)) {
			agentConfigFile = new File(agentConfigLocation);
		}

		if (agentConfigFile.exists()) {
			try {
				loadExternalProperties(agentConfigFile);
			} catch (Exception e) {
				LOG.error("Error loading properties from {}", agentConfigFile.getAbsolutePath());
				loadDefaultProperties();
			}
		} else {
			loadDefaultProperties();
		}
	}

	public Config(Properties properties) {
		this.properties = properties;
	}

	protected File getJarLocation() {
		try {
			return new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (Exception e) {
			return null;
		}
	}

	protected void loadExternalProperties(File agentConfigFile) throws Exception {
		InputStream agentConfigInputStream = FileUtils.openInputStream(agentConfigFile);
		try {
			properties.load(agentConfigInputStream);
		} finally {
			IOUtils.closeQuietly(agentConfigInputStream);
		}
	}

	private void loadDefaultProperties() {
		LOG.info("Loading default properties");
		try {
			properties.load(Config.class.getResourceAsStream("default.properties"));
		} catch (IOException e) {
			LOG.error("Error loading default properties");
		}
	}

	public static final String AGENT_BASE_PORT = "agent.base.port";
	public static final String DEFAULT_AGENT_BASE_PORT = "21000";

	public static final String AGENT_HOST = "agent.host";

	public static final String AUTORUN = "agent.autorun";
	public static final String DEFAULT_AUTORUN = "rest,job,ping";

	public static final String AGENT_FRIENDS = "agent.friends";

	public static final String LIST_SEPARATOR = ",";

	public static final String AGENT_ROOT_PATH = "agent.root.path";

	public static final String AGENT_APP_PATH = "agent.app.path";
	public static final String DEFAULT_AGENT_APP_PATH = "agent";

	public static final String AGENT_FILES_PATH = "agent.files.path";
	public static final String DEFAULT_AGENT_FILES_PATH = "files";

	public static final String AGENT_DATABASE_PATH = "agent.db.path";
	public static final String DEFAULT_AGENT_DATABASE_PATH = "db";	
	
	public static final String HTTP_PROXY_HOST = "agent.http.proxy.host";
	public static final String HTTP_PROXY_PORT = "agent.http.proxy.port";
	public static final String DEFAULT_HTTP_PROXY_PORT = "3128";

	public static final String JOB_POOL_SIZE = "agent.job.pool.size";
	public static final String DEFAULT_JOB_POOL_SIZE = "4";

	public static final String PING_POOL_SIZE = "agent.ping.pool.size";
	public static final String DEFAULT_PING_POOL_SIZE = "2";

	public static final String CDN_POOL_SIZE = "agent.cdn.pool.size";
	public static final String DEFAULT_CDN_POOL_SIZE = "4";

	public static final String CDN_TIMEOUT = "agent.cdn.timeout";
	public static final String DEFAULT_CDN_TIMEOUT = "3000";
	
	public static final String SERVER_TIMEOUT = "agent.server.timeout";
	public static final String DEFAULT_SERVER_TIMEOUT = "30";

	public static final String HTTP_CLIENT_TIMEOUT = "agent.httpclient.timeout";
	public static final String DEFAULT_HTTP_CLIENT_TIMEOUT = "5000";
	
	
	
	public static final String KEYSTORE_PATH = "agent.keystore.path";

	public static final String KEYSTORE_STORE_PASSWORD = "agent.keystore.storepass";
	public static final String DEFAULT_KEYSTORE_STORE_PASSWORD = "123456";

	public static final String KEYSTORE_KEY_PASSWORD = "agent.keystore.keypass";
	public static final String DEFAULT_KEYSTORE_KEY_PASSWORD = "123456";

	public static final String AGENT_REST_SECURED = "agent.rest.secured";
	public static final String AGENT_REST_LOGIN = "agent.rest.login";
	public static final String AGENT_REST_PASSWORD = "agent.rest.password";
	public static final String AGENT_REST_REALM = "agent.rest.realm";
	public static final String DEFAULT_AGENT_REST_REALM = "Agent";

	public static final String AGENT_FFMPEG_PATH = "agent.ffmpeg.path";	
	
	public static final String TWITTER_CONSUMER_KEY = "agent.twitter.key";
	public static final String TWITTER_CONSUMER_SECRET = "agent.twitter.secret";
	
	
	public String get(String key) {
		return properties.getProperty(key);
	}

	public Properties getProperties() {
		return properties;
	}

	public boolean isRestSecured() {
		return Boolean.valueOf(properties.getProperty(AGENT_REST_SECURED));
	}

	public String getRestLogin() {
		return properties.getProperty(AGENT_REST_LOGIN, "");
	}

	public String getRestPassword() {
		return properties.getProperty(AGENT_REST_PASSWORD, "");
	}

	public String getRestRealm() {
		return properties.getProperty(AGENT_REST_REALM, DEFAULT_AGENT_REST_REALM);
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
		return autorun.split(LIST_SEPARATOR);
	}

	public String[] getAgentFriends() {
		String friendIds = properties.getProperty(AGENT_FRIENDS, "").trim();
		return friendIds.split(LIST_SEPARATOR);
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

	public int getCdnPort() {
		return getBasePort() + CdnServer.CDN_PORT;
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
		String agentAppPath = properties.getProperty(AGENT_APP_PATH);
		if (StringUtils.nullOrEmpty(agentAppPath)) {
			agentAppPath = getAgentRootPath() + File.separatorChar + DEFAULT_AGENT_APP_PATH;
		}
		return agentAppPath;
	}

	public String getAgentFilesPath() {
		String agentFilesPath = properties.getProperty(AGENT_FILES_PATH);
		if (StringUtils.nullOrEmpty(agentFilesPath)) {
			agentFilesPath = getAgentAppPath() + File.separatorChar + DEFAULT_AGENT_FILES_PATH;
		}
		return agentFilesPath;
	}

	public String getAgentDatabasePath() {
		String agentDatabasePath = properties.getProperty(AGENT_DATABASE_PATH);
		if (StringUtils.nullOrEmpty(agentDatabasePath)) {
			agentDatabasePath = getAgentAppPath() + File.separatorChar + DEFAULT_AGENT_DATABASE_PATH;
		}
		return agentDatabasePath;
	}	
	
	public int getJobPoolSize() {
		return Integer.parseInt(properties.getProperty(JOB_POOL_SIZE, DEFAULT_JOB_POOL_SIZE));
	}

	public int getPingPoolSize() {
		return Integer.parseInt(properties.getProperty(PING_POOL_SIZE, DEFAULT_PING_POOL_SIZE));
	}

	public int getCdnPoolSize() {
		return Integer.parseInt(properties.getProperty(CDN_POOL_SIZE, DEFAULT_CDN_POOL_SIZE));
	}

	public int getCdnTimeout() {
		return Integer.parseInt(properties.getProperty(CDN_TIMEOUT, DEFAULT_CDN_TIMEOUT));
	}

	public int getServerTimeout() {
		return Integer.parseInt(properties.getProperty(SERVER_TIMEOUT, DEFAULT_SERVER_TIMEOUT));
	}	

	public int getHttpClientTimeout() {
		return Integer.parseInt(properties.getProperty(HTTP_CLIENT_TIMEOUT, DEFAULT_HTTP_CLIENT_TIMEOUT));
	}	
	
	public String getFfmpegPath() {
		return properties.getProperty(AGENT_FFMPEG_PATH);
	}

	public String getTwitterConsumerKey() {
		return properties.getProperty(TWITTER_CONSUMER_KEY);
	}	

	public String getTwitterConsumerSecret() {
		return properties.getProperty(TWITTER_CONSUMER_SECRET);
	}	
	
	@Override
	public String toString() {
		return StringUtils.printProperties(properties, null);
	}

}
