package agent;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

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
		assertArrayEquals(new String[] { Configuration.DEFAULT_AUTORUN }, config.getAutorunServices());

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
	public void testGetDownloadPath() {
		// empty - default
		assertEquals(SystemUtils.getUserHome(), config.getDownloadPath());
		// changed
		String downloadPath = "/home/agent";
		properties.put(Configuration.DOWNLOAD_PATH, downloadPath);
		assertEquals(downloadPath, config.getDownloadPath());
	}

	@Test
	public void testGetRestUploadPath() {
		// empty - default
		assertEquals(SystemUtils.getUserHome(), config.getRestUploadPath());
		// changed
		String restUploadPath = "/home/agent";
		properties.put(Configuration.REST_UPLOAD_PATH, restUploadPath);
		assertEquals(restUploadPath, config.getRestUploadPath());
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
		assertEquals(0, config.getHttpProxyPort());
		// changed
		int proxyPort = 3128;
		properties.put(Configuration.HTTP_PROXY_PORT, String.valueOf(proxyPort));
		assertEquals(proxyPort, config.getHttpProxyPort());
	}

	@Test
	public void testGetProperties() {
		assertEquals(properties, config.getProperties());
	}

	@Test
	public void testGetRestPort() {
		// default
		assertEquals(Integer.parseInt(Configuration.DEFAULT_AGENT_BASE_PORT) + Integer.parseInt(Configuration.DEFAULT_REST_PORT), config.getRestPort());
		// changed
		int restPort = 8080;
		properties.put(Configuration.REST_PORT, String.valueOf(restPort));
		assertEquals(Integer.parseInt(Configuration.DEFAULT_AGENT_BASE_PORT) + restPort, config.getRestPort());
	}

	@Test
	public void testGetTelnetPort() {
		// default
		assertEquals(Integer.parseInt(Configuration.DEFAULT_AGENT_BASE_PORT) + Integer.parseInt(Configuration.DEFAULT_TELNET_PORT), config.getTelnetPort());
		// changed
		int telnetPort = 8023;
		properties.put(Configuration.TELNET_PORT, String.valueOf(telnetPort));
		assertEquals(Integer.parseInt(Configuration.DEFAULT_AGENT_BASE_PORT) + telnetPort, config.getTelnetPort());
	}

	@Test
	public void testIsRestSsl() {
		assertFalse(config.isRestSsl());
	}

}
