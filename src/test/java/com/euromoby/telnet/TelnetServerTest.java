package com.euromoby.telnet;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.Config;

@RunWith(MockitoJUnitRunner.class)
public class TelnetServerTest {

	@Mock
	Config config;
	@Mock
	TelnetServerInitializer telnetServerInitializer;

	TelnetServer server;

	@Before
	public void init() {
		Mockito.when(config.getBasePort()).thenReturn(Integer.parseInt(Config.DEFAULT_AGENT_BASE_PORT));
		server = new TelnetServer(config, telnetServerInitializer);
	}

	@Test
	public void testGetServiceName() {
		assertEquals(TelnetServer.SERVICE_NAME, server.getServiceName());
	}

}
