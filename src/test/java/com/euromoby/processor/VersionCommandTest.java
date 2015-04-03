package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.agent.Agent;

public class VersionCommandTest {

	VersionCommand versionCommand;

	@Before
	public void init() {
		versionCommand = new VersionCommand();
	}

	@Test
	public void testMatchName() {
		assertTrue(versionCommand.match(VersionCommand.NAME));
	}
	
	@Test
	public void testNotMatch() {
		assertFalse(versionCommand.match(VersionCommand.NAME + "aaa"));
	}

	@Test
	public void testDefault() {
		String result = versionCommand.execute(versionCommand.name());
		assertEquals(String.format(VersionCommand.OUTPUT, Agent.TITLE, Agent.VERSION), result);
	}
	
}
