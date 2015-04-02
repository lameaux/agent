package com.euromoby.agent;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class AgentSpringConfigTest {

	private AgentSpringConfig agentSpringConfig;

	@Before
	public void init() {
		agentSpringConfig = new AgentSpringConfig();
	}

	@Test
	public void testGetConfig() {
		assertNotNull(agentSpringConfig.config());
	}

}
