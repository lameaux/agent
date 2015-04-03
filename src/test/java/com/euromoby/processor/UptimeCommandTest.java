package com.euromoby.processor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class UptimeCommandTest {

	UptimeCommand uptimeCommand;

	@Before
	public void init() {
		uptimeCommand = new UptimeCommand();
	}

	@Test
	public void testMatchName() {
		assertTrue(uptimeCommand.match(UptimeCommand.NAME));
	}
	
	@Test
	public void testNotMatch() {
		assertFalse(uptimeCommand.match(UptimeCommand.NAME + "aaa"));
	}

	@Test
	public void testDefault() {
		String result = uptimeCommand.execute(uptimeCommand.name());
		assertTrue(result.matches("[0-9]+ days [0-9]+ hours [0-9]+ minutes [0-9]+ seconds"));
	}
	
}
