package com.euromoby.agent;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class SpringConfigTest {

	private SpringConfig agentSpringConfig;

	@Before
	public void init() {
		agentSpringConfig = new SpringConfig();
	}

	@Test
	public void testGetConfig() {
		assertNotNull(agentSpringConfig.config());
	}

}
