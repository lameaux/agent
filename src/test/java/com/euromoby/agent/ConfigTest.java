package com.euromoby.agent;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.model.AgentId;
import com.euromoby.utils.NetUtils;
import com.euromoby.utils.SystemUtils;


public class ConfigTest {

	private Properties properties;
	private Config config;

	private static final String DUMMY = "dummy";	
	private static final String DUMMY_STR = "dummy";
	private static final int DUMMY_INT = 42;
	
	@Before
	public void init() {
		properties = new Properties();
		config = new Config(properties);
	}

	@Test
	public void testGet() {
		// empty - default
		assertNull(config.get(DUMMY));
		// changed
		properties.put(DUMMY, DUMMY_STR);
		assertEquals(DUMMY_STR, config.get(DUMMY));
	}

	@Test
	public void testGetJarLocation() throws Exception {
		File jarLocation = config.getJarLocation();
		assertEquals(new File(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().toURI(), jarLocation.toURI());
	}
	
	@Test
	public void testGetProperties() {
		assertEquals(properties, config.getProperties());
	}
	
	@Test
	public void testGetHost() {
		assertEquals(NetUtils.getHostname(), config.getHost());
		properties.put(Config.AGENT_HOST, DUMMY_STR);
		assertEquals(DUMMY_STR, config.getHost());
	}

	@Test
	public void testGetAgentId() {
		AgentId agentId = new AgentId(config.getHost(), config.getBasePort());
		assertEquals(agentId, config.getAgentId());
	}
	
	@Test
	public void testGetAutorunServices() {
		// empty - default
		assertArrayEquals(Config.DEFAULT_AUTORUN.split(",") , config.getAutorunServices());

		// changed
		String autorun = "telnet,rest";
		properties.put(Config.AUTORUN, autorun);
		String[] autoruns = autorun.split(",");

		assertArrayEquals(autoruns, config.getAutorunServices());
	}

	@Test
	public void testBasePort() {
		// empty - default
		assertEquals(Integer.parseInt(Config.DEFAULT_AGENT_BASE_PORT), config.getBasePort());
		// changed
		properties.put(Config.AGENT_BASE_PORT, String.valueOf(DUMMY_INT));
		assertEquals(DUMMY_INT, config.getBasePort());
	}

	@Test
	public void testGetKeystorePath() {
		assertNull(config.getKeystorePath());
		properties.put(Config.KEYSTORE_PATH, DUMMY_STR);
		assertEquals(DUMMY_STR, config.getKeystorePath());
	}	

	@Test
	public void testGetKeystoreStorePass() {
		// empty - default
		assertEquals(Config.DEFAULT_KEYSTORE_STORE_PASSWORD, config.getKeystoreStorePass());		
		//changed
		properties.put(Config.KEYSTORE_STORE_PASSWORD, DUMMY_STR);
		assertEquals(DUMMY_STR, config.getKeystoreStorePass());		
	}
	
	@Test
	public void testGetKeystoreKeyPass() {
		// empty - default
		assertEquals(Config.DEFAULT_KEYSTORE_KEY_PASSWORD, config.getKeystoreKeyPass());		
		//changed
		properties.put(Config.KEYSTORE_KEY_PASSWORD, DUMMY_STR);
		assertEquals(DUMMY_STR, config.getKeystoreKeyPass());		
	}
	
	@Test
	public void testGetHttpProxyHost() {
		// default
		assertNull(config.getHttpProxyHost());
		// changed
		properties.put(Config.HTTP_PROXY_HOST, DUMMY_STR);
		assertEquals(DUMMY_STR, config.getHttpProxyHost());
	}

	@Test
	public void testGetHttpProxyPort() {
		// default
		assertEquals(Integer.parseInt(Config.DEFAULT_HTTP_PROXY_PORT), config.getHttpProxyPort());
		// changed
		properties.put(Config.HTTP_PROXY_PORT, String.valueOf(DUMMY_INT));
		assertEquals(DUMMY_INT, config.getHttpProxyPort());
	}	

	@Test
	public void testIsHttpProxy() {
		// default
		assertFalse(config.isHttpProxy());
		// changed
		properties.put(Config.HTTP_PROXY_HOST, DUMMY_STR);		
		assertTrue(config.isHttpProxy());
	}	
	
	@Test
	public void testGetAgentRootPath() {
		// empty - default
		assertEquals(SystemUtils.getUserHome(), config.getAgentRootPath());		
		//changed
		properties.put(Config.AGENT_ROOT_PATH, DUMMY_STR);
		assertEquals(DUMMY_STR, config.getAgentRootPath());		
	}

	@Test
	public void testGetAgentAppPath() {
		// empty - default
		assertEquals(config.getAgentRootPath() + File.separatorChar + Config.DEFAULT_AGENT_APP_PATH , config.getAgentAppPath());		
		//changed
		properties.put(Config.AGENT_APP_PATH, DUMMY_STR);
		assertEquals(DUMMY_STR, config.getAgentAppPath());		
	}
	
	@Test
	public void testGetAgentFilesPath() {
		// empty - default
		assertEquals(config.getAgentAppPath() + File.separatorChar + Config.DEFAULT_AGENT_FILES_PATH, config.getAgentFilesPath());
		// changed
		properties.put(Config.AGENT_FILES_PATH, DUMMY_STR);
		assertEquals(DUMMY_STR, config.getAgentFilesPath());
	}

	@Test
	public void testGetJobPoolSize() {
		// default
		assertEquals(Integer.parseInt(Config.DEFAULT_JOB_POOL_SIZE), config.getJobPoolSize());
		// changed
		properties.put(Config.JOB_POOL_SIZE, String.valueOf(DUMMY_INT));
		assertEquals(DUMMY_INT, config.getJobPoolSize());
	}

	@Test
	public void testGetPingPoolSize() {
		// default
		assertEquals(Integer.parseInt(Config.DEFAULT_PING_POOL_SIZE), config.getPingPoolSize());
		// changed
		properties.put(Config.PING_POOL_SIZE, String.valueOf(DUMMY_INT));
		assertEquals(DUMMY_INT, config.getPingPoolSize());
	}	
	
}
