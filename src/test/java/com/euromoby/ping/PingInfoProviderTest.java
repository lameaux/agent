package com.euromoby.ping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.Config;
import com.euromoby.model.AgentId;
import com.euromoby.ping.model.PingInfo;
import com.euromoby.utils.SystemUtils;

@RunWith(MockitoJUnitRunner.class)
public class PingInfoProviderTest {
	
	@Mock
	Config config;
	PingInfoProvider pingInfoProvider;

	@Before
	public void init() {
		pingInfoProvider = new PingInfoProvider(config);
	}
	@Test
	public void testCreatePingInfo() {
		AgentId agentId = new AgentId("host:12345");
		Mockito.when(config.getAgentId()).thenReturn(agentId);
		Mockito.when(config.getAgentFilesPath()).thenReturn(SystemUtils.getUserHome());
		PingInfo pingInfo = pingInfoProvider.createPingInfo();
		assertEquals(agentId, pingInfo.getAgentId());
		assertTrue(pingInfo.getFreeSpace() > 0);
	}

}
