package com.euromoby.agent.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.agent.model.AgentStatus;

public class AgentStatusTest {

	private AgentStatus agentStatus;
	
	@Before
	public void init() {
		agentStatus = new AgentStatus();
	}	
	
	@Test
	public void testMyHost() {
		String MYHOST = "myhost";
		agentStatus.setMyHost(MYHOST);
		assertEquals(MYHOST, agentStatus.getMyHost());
	}
	
	@Test
	public void testLastPingSendAttempt() {
		long TIME = System.currentTimeMillis();
		agentStatus.setLastPingSendAttempt(TIME);
		assertEquals(TIME, agentStatus.getLastPingSendAttempt());
	}
	
	@Test
	public void testLastPingSendSuccess() {
		long TIME = System.currentTimeMillis();
		agentStatus.setLastPingSendSuccess(TIME);
		assertEquals(TIME, agentStatus.getLastPingSendSuccess());
	}
	
	@Test
	public void testLastPingReceived() {
		long TIME = System.currentTimeMillis();
		agentStatus.setLastPingReceived(TIME);
		assertEquals(TIME, agentStatus.getLastPingReceived());
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
