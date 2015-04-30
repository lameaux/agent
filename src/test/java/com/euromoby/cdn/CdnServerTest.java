package com.euromoby.cdn;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.Config;

@RunWith(MockitoJUnitRunner.class)
public class CdnServerTest {

	@Mock
	Config config;
	@Mock
	CdnServerInitializer cdnServerInitializer;

	CdnServer server;

	@Before
	public void init() {
		Mockito.when(config.getCdnPort()).thenReturn(Integer.parseInt(Config.DEFAULT_AGENT_BASE_PORT) + CdnServer.CDN_PORT);
		server = new CdnServer(config, cdnServerInitializer);
	}

	@Test
	public void testGetServiceName() {
		assertEquals(CdnServer.SERVICE_NAME, server.getServiceName());
	}

}
