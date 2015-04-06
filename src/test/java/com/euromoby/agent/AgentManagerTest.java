package com.euromoby.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.model.AgentId;
import com.euromoby.model.PingInfo;

public class AgentManagerTest {

	private Properties properties;
	private Config config;	
	private AgentManager agentManager;
	private AgentId agent1;
	private AgentId agent2;
	private AgentId agent3;		
	private AgentId unknownAgent;	
	
	@Before
	public void init() {
		properties = new Properties();
		config = new Config(properties);		
		agentManager = new AgentManager(config);
		agent1 = new AgentId("host1:21000");
		agent2 = new AgentId("host2:21000");
		agent3 = new AgentId("host3:21000");		
		unknownAgent = new AgentId("unknownAgent:21000");
	}	
	
	@Test
	public void testAddAgentAndGetAgentStatus() {
		// no status
		assertNull(agentManager.getAgentStatus(agent1));
		// add new
		agentManager.addAgent(agent1);
		AgentStatus agentStatus = agentManager.getAgentStatus(agent1);
		assertNotNull(agentStatus);
		// add again
		agentManager.addAgent(agent1);
		assertEquals(agentStatus, agentManager.getAgentStatus(agent1));
		
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
	
	@Test
	public void testNotifyPingReceiveNulls() {
		// should do nothing
		agentManager.notifyPingReceive(null);
		agentManager.notifyPingReceive(new PingInfo(unknownAgent));
		assertNull(agentManager.getAgentStatus(unknownAgent));
	}

	@Test
	public void testNotifyPingSendSuccessNulls() {
		// should do nothing
		agentManager.notifyPingSendSuccess(null);
		agentManager.notifyPingSendSuccess(new PingInfo(unknownAgent));
		assertNull(agentManager.getAgentStatus(unknownAgent));
	}	
	
	@Test
	public void testNotifyPingSendAttempt() {
		// should do nothing
		agentManager.notifyPingSendAttempt(unknownAgent);
		assertNull(agentManager.getAgentStatus(unknownAgent));
	}
	
	@Test
	public void testAfterPropertiesSet() throws Exception {
		AgentId MY_HOST = new AgentId("myhost:21000");
		AgentId FRIEND_HOST = new AgentId("friendhost:22000");
		String INVALID_AGENT = "invalidhost";
		
		properties.setProperty(Config.AGENT_HOST, MY_HOST.getHost());
		properties.setProperty(Config.AGENT_BASE_PORT, String.valueOf(MY_HOST.getBasePort()));
		properties.setProperty(Config.AGENT_FRIENDS, MY_HOST.toString() + Config.LIST_SEPARATOR + FRIEND_HOST.toString() + Config.LIST_SEPARATOR + INVALID_AGENT);
		agentManager.afterPropertiesSet();
		
		List<AgentId> agents = agentManager.getAll();
		assertEquals(1, agents.size());
		assertFalse(agents.contains(MY_HOST));
		assertTrue(agents.contains(FRIEND_HOST));
		
	}
	
}
