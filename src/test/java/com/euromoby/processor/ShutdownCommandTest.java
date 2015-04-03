package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.agent.Agent;

public class ShutdownCommandTest {

	ShutdownCommand shutdownCommand;

	@Before
	public void init() {
		shutdownCommand = new ShutdownCommand();
	}

	@Test
	public void testMatchName() {
		assertTrue(shutdownCommand.match(ShutdownCommand.NAME));
	}
	
	@Test
	public void testNotMatch() {
		assertFalse(shutdownCommand.match(ShutdownCommand.NAME + "aaa"));
	}

	@Test
	public void testDefault() {
		assertTrue(Agent.isRunning());
		String result = shutdownCommand.execute(shutdownCommand.name());
		assertFalse(Agent.isRunning());
		assertEquals(result, ShutdownCommand.GOODBYE);
	}
	
}
