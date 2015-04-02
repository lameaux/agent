package com.euromoby.agent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.model.AgentId;
import com.euromoby.model.PingInfo;

public class AgentManagerTest {

	private AgentManager agentManager;
	private AgentId agent1;
	private AgentId agent2;
	private AgentId agent3;		
	
	@Before
	public void init() {
		agentManager = new AgentManager();
		agent1 = new AgentId("host1:21000");
		agent2 = new AgentId("host2:21000");
		agent3 = new AgentId("host3:21000");		
	}	
	
	@Test
	public void testAddAgentAndGetAgentStatus() {
		// no status
		assertNull(agentManager.getAgentStatus(agent1));
		// add new
		agentManager.addAgent(agent1);
		assertNotNull(agentManager.getAgentStatus(agent1));
	}

	@Test
	public void testGetAll() {
		// empty
		List<AgentId> list = agentManager.getAll();
		assertTrue(list.isEmpty());
		// add new
		agentManager.addAgent(agent1);
		agentManager.addAgent(agent2);
		agentManager.addAgent(agent3);	
		// check
		list = agentManager.getAll();
		assertTrue(list.contains(agent1));
		assertTrue(list.contains(agent2));
		assertTrue(list.contains(agent3));		
	}	

	@Test
	public void testGetActive() {
		// empty
		List<AgentId> list = agentManager.getActive();
		assertTrue(list.isEmpty());
		// add new
		agentManager.addAgent(agent1);
		agentManager.addAgent(agent2);
		agentManager.addAgent(agent3);	
		// check empty 
		list = agentManager.getActive();
		assertFalse(list.contains(agent1));
		assertFalse(list.contains(agent2));
		assertFalse(list.contains(agent3));		
		// ping success to agent1
		PingInfo pingInfo1 = new PingInfo(agent1);
		agentManager.notifyPingSendSuccess(pingInfo1);
		// receive ping from ping2
		PingInfo pingInfo2 = new PingInfo(agent2);
		agentManager.notifyPingReceive(pingInfo2);
		// check
		list = agentManager.getActive();
		assertTrue(list.contains(agent1));
		assertTrue(list.contains(agent2));
		assertFalse(list.contains(agent3));		
	}	

	@Test
	public void testGetAllForPing() {
		// empty
		List<AgentId> list = agentManager.getAllForPing();
		assertTrue(list.isEmpty());
		// add new
		agentManager.addAgent(agent1);
		agentManager.addAgent(agent2);
		agentManager.addAgent(agent3);	
		// check all new
		list = agentManager.getAllForPing();
		assertTrue(list.contains(agent1));
		assertTrue(list.contains(agent2));
		assertTrue(list.contains(agent3));			
		// ping attempt to agent1
		agentManager.notifyPingSendAttempt(agent1);
		// ping attempt to agent2
		agentManager.notifyPingSendAttempt(agent2);
		// check
		list = agentManager.getAllForPing();
		assertFalse(list.contains(agent1));
		assertFalse(list.contains(agent2));
		assertTrue(list.contains(agent3));		
	}	
	
}
