package com.euromoby.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.agent.Config;
import com.euromoby.http.SSLContextProvider;

public class SSLContextProviderTest {

	private Properties properties;
	private Config config;
	private SSLContextProvider sslContextProvider;
	
	@Before
	public void init() {
		properties = new Properties();
		config = new Config(properties);
		sslContextProvider = new SSLContextProvider(config);
	}	
	
	@Test
	public void testNotInitialized() {
		assertNull(sslContextProvider.getSSLContext());
		try {
			sslContextProvider.newServerSSLEngine();
			fail("exception is expected");
		} catch (IllegalStateException e){}
	}

	@Test
	public void testInitialized() {
		try {
			sslContextProvider.afterPropertiesSet();
		} catch (Exception e) {
			fail(e.toString());
		}
		
		SSLContext sslContext = sslContextProvider.getSSLContext();
		assertNotNull(sslContext);
		
		SSLEngine sslEngine = sslContextProvider.newServerSSLEngine();
		assertFalse(sslEngine.getUseClientMode());
		
	}
	
	
}
