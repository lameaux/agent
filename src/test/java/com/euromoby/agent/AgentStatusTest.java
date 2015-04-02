package com.euromoby.agent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class AgentStatusTest {

	private AgentStatus agentStatus;
	
	@Before
	public void init() {
		agentStatus = new AgentStatus();
	}	
	
	@Test
	public void testIsActive() {
		// default
		assertFalse(agentStatus.isActive());
		// ping response received
		agentStatus.setLastPingSendSuccess(System.currentTimeMillis());
		assertTrue(agentStatus.isActive());
		// reset
		agentStatus.setLastPingSendSuccess(0);		
		assertFalse(agentStatus.isActive());
		// I was pinged
		agentStatus.setLastPingReceived(System.currentTimeMillis());		
		assertTrue(agentStatus.isActive());		
	}

	@Test
	public void testIsPingRequired() {
		// default
		assertTrue(agentStatus.isPingRequired());
		// ping request was sent
		agentStatus.setLastPingSendAttempt(System.currentTimeMillis());
		assertFalse(agentStatus.isPingRequired());		
	}
	
}
