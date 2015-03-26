package agent;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import rest.RestServer;
import telnet.TelnetServer;
import utils.SystemUtils;

public class ConfigurationTest {

	private Properties properties;
	private Configuration config;

	@Before
	public void init() {
		properties = new Properties();
		config = new Configuration(properties);
	}

	@Test
	public void testGet() {
		// empty - default
		String PARAM1 = "PARAM1";
		String VALUE1 = "VALUE1";
		assertNull(config.get(PARAM1));
		// changed
		properties.put(PARAM1, VALUE1);
		assertEquals(VALUE1, config.get(PARAM1));
	}

	@Test
	public void testGetAutorunServices() {
		// empty - default
		assertArrayEquals(Configuration.DEFAULT_AUTORUN.split(",") , config.getAutorunServices());

		// changed
		String autorun = "telnet,rest";
		properties.put(Configuration.AUTORUN, autorun);
		String[] autoruns = autorun.split(",");

		assertArrayEquals(autoruns, config.getAutorunServices());
	}

	@Test
	public void testBasePort() {
		// empty - default
		assertEquals(Integer.parseInt(Configuration.DEFAULT_AGENT_BASE_PORT), config.getBasePort());
		// changed
		int baseport = 1000;
		properties.put(Configuration.AGENT_BASE_PORT, String.valueOf(baseport));
		assertEquals(baseport, config.getBasePort());
	}

	@Test
	public void testGetAgentRootPath() {
		// empty - default
		assertEquals(SystemUtils.getUserHome(), config.getAgentRootPath());		
		//changed
		String agentRootPath = "/home/agent";
		properties.put(Configuration.AGENT_ROOT_PATH, agentRootPath);
		assertEquals(agentRootPath, config.getAgentRootPath());		
	}

	@Test
	public void testGetAgentAppPath() {
		// empty - default
		assertEquals(config.getAgentRootPath() + File.separatorChar + Configuration.DEFAULT_AGENT_APP_PATH , config.getAgentAppPath());		
		//changed
		String agentAppPath = "agentapp";
		properties.put(Configuration.AGENT_APP_PATH, agentAppPath);
		assertEquals(config.getAgentRootPath() + File.separatorChar + agentAppPath, config.getAgentAppPath());		
	}
	
	
	@Test
	public void testGetAgentFilesPath() {
		// empty - default
		assertEquals(config.getAgentAppPath() + File.separatorChar + Configuration.DEFAULT_AGENT_FILES_PATH, config.getAgentFilesPath());
		// changed
		String filesPath = "cdnfiles";
		properties.put(Configuration.AGENT_FILES_PATH, filesPath);
		assertEquals(config.getAgentAppPath() + File.separatorChar + filesPath, config.getAgentFilesPath());
	}

	@Test
	public void testGetHttpProxyHost() {
		// default
		assertNull(config.getHttpProxyHost());
		assertFalse(config.isHttpProxy());
		// changed
		String proxyHost = "proxy";
		properties.put(Configuration.HTTP_PROXY_HOST, proxyHost);
		assertEquals(proxyHost, config.getHttpProxyHost());
		assertTrue(config.isHttpProxy());
	}

	@Test
	public void testGetHttpProxyPort() {
		// default
		assertEquals(3128, config.getHttpProxyPort());
		// changed
		int proxyPort = 8080;
		properties.put(Configuration.HTTP_PROXY_PORT, String.valueOf(proxyPort));
		assertEquals(proxyPort, config.getHttpProxyPort());
	}

	@Test
	public void testGetProperties() {
		assertEquals(properties, config.getProperties());
	}

	@Test
	public void testGetRestPort() {
		assertEquals(Integer.parseInt(Configuration.DEFAULT_AGENT_BASE_PORT) + RestServer.REST_PORT, config.getRestPort());
	}

	@Test
	public void testGetTelnetPort() {
		assertEquals(Integer.parseInt(Configuration.DEFAULT_AGENT_BASE_PORT) + TelnetServer.TELNET_PORT, config.getTelnetPort());
	}

	@Test
	public void testIsRestSsl() {
		assertFalse(config.isRestSsl());
	}

}
