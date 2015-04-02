package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.AgentManager;
import com.euromoby.model.AgentId;

@RunWith(MockitoJUnitRunner.class)
public class AgentCommandTest {

	@Mock
	AgentManager agentManager;
	AgentCommand agentCommand;

	@Before
	public void init() {
		agentCommand = new AgentCommand(agentManager);
	}

	@Test
	public void testMatchName() {
		assertTrue(agentCommand.match(AgentCommand.NAME));
	}

	@Test
	public void testMatchWithParam() {
		assertTrue(agentCommand.match(AgentCommand.NAME + CommandBase.COMMAND_SEPARATOR + AgentCommand.PARAM_ACTIVE));
		assertTrue(agentCommand.match(AgentCommand.NAME + CommandBase.COMMAND_SEPARATOR + AgentCommand.PARAM_ALL));
		assertTrue(agentCommand.match(AgentCommand.NAME + CommandBase.COMMAND_SEPARATOR + AgentCommand.PARAM_ADD));		
	}
	
	@Test
	public void testNotMatch() {
		assertFalse(agentCommand.match(AgentCommand.NAME + "aaa"));
	}

	@Test
	public void testBadRequest() {
		String result = agentCommand.execute(AgentCommand.NAME + CommandBase.COMMAND_SEPARATOR + "aaa");
		assertEquals(agentCommand.syntaxError(), result);
	}

	@Test
	public void testDefault() {
		agentCommand.execute(agentCommand.name() + CommandBase.COMMAND_SEPARATOR);
		Mockito.verify(agentManager).getActive();
	}

	@Test
	public void testActive() {
		agentCommand.execute(agentCommand.name() + CommandBase.COMMAND_SEPARATOR + AgentCommand.PARAM_ACTIVE);
		Mockito.verify(agentManager).getActive();
	}	

	@Test
	public void testAll() {
		agentCommand.execute(agentCommand.name() + CommandBase.COMMAND_SEPARATOR + AgentCommand.PARAM_ALL);
		Mockito.verify(agentManager).getAll();
	}	

	@Test
	public void testAddEmpty() {
		String result = agentCommand.execute(agentCommand.name() + CommandBase.COMMAND_SEPARATOR + AgentCommand.PARAM_ADD);
		assertEquals(agentCommand.syntaxError(), result);
	}	
	
	@Test
	public void testAdd() {
		String host = "host";
		int baseport = 21000;
		AgentId agentId = new AgentId(host, baseport);
		agentCommand.execute(agentCommand.name() + CommandBase.COMMAND_SEPARATOR + AgentCommand.PARAM_ADD + CommandBase.COMMAND_SEPARATOR + host + CommandBase.COMMAND_SEPARATOR + baseport );
		Mockito.verify(agentManager).addAgent(Matchers.eq(agentId));
	}	
	
}
