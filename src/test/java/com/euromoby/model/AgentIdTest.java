package com.euromoby.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class AgentIdTest {

	@Test
	public void testConstructors() {
		assertEquals(new AgentId("host", 21000), new AgentId("host:21000"));
	}

	@Test
	public void testNullConstructors() {
		try {
			new AgentId(null);
			fail();
		} catch (IllegalArgumentException iae) {
		}
		try {
			new AgentId(null, 0);
			fail();
		} catch (IllegalArgumentException iae) {
		}
	}
	
	@Test
	public void testInvalidConstructors() {
		try {
			new AgentId("noport");
			fail();
		} catch (IllegalArgumentException iae) {
		}
		try {
			new AgentId("host:invalidport");
			fail();
		} catch (IllegalArgumentException iae) {
		}
		try {
			new AgentId(":0000");
			fail();
		} catch (IllegalArgumentException iae) {
		}
		
	}
	
	
}
