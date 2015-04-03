package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.model.AgentId;
import com.euromoby.model.PingInfo;
import com.euromoby.ping.PingSender;

@RunWith(MockitoJUnitRunner.class)
public class PingCommandTest {

	private static final String HOST = "localhost";
	private static final int REST_PORT = 21443;

	@Mock
	PingSender pingSender;
	PingCommand pingCommand;

	@Before
	public void init() {
		pingCommand = new PingCommand(pingSender);
	}

	@Test
	public void testMatchName() {
		assertTrue(pingCommand.match(PingCommand.NAME));
	}

	@Test
	public void testMatchWithParam() {
		assertTrue(pingCommand.match(PingCommand.NAME + Command.SEPARATOR + HOST + Command.SEPARATOR + REST_PORT));
		assertTrue(pingCommand.match(PingCommand.NAME + Command.SEPARATOR + HOST + Command.SEPARATOR + REST_PORT + Command.SEPARATOR + PingCommand.NO_PROXY));
	}

	@Test
	public void testNotMatch() {
		assertFalse(pingCommand.match(PingCommand.NAME + "aaa"));
	}

	@Test
	public void testBadRequest() {
		String result = pingCommand.execute(PingCommand.NAME + Command.SEPARATOR + "aaa");
		assertEquals(pingCommand.syntaxError(), result);
	}

	@Test
	public void testWithProxy() throws Exception {
		AgentId agentId = new AgentId("host", 21000);
		PingInfo pingInfo = new PingInfo(agentId);
		Mockito.when(pingSender.ping(HOST, REST_PORT, false)).thenReturn(pingInfo);
		String result = pingCommand.execute(pingCommand.name() + Command.SEPARATOR + HOST + Command.SEPARATOR + REST_PORT);
		assertTrue(result.startsWith(agentId.toString()));
	}

	@Test
	public void testWithoutProxy() throws Exception {
		AgentId agentId = new AgentId("host", 21000);
		PingInfo pingInfo = new PingInfo(agentId);
		Mockito.when(pingSender.ping(HOST, REST_PORT, true)).thenReturn(pingInfo);
		String result = pingCommand.execute(pingCommand.name() + Command.SEPARATOR + HOST + Command.SEPARATOR + REST_PORT + Command.SEPARATOR
				+ PingCommand.NO_PROXY);
		assertTrue(result.startsWith(agentId.toString()));
	}

	@Test
	public void testWithException() throws Exception {
		Mockito.when(pingSender.ping(HOST, REST_PORT, false)).thenThrow(new Exception());
		String result = pingCommand.execute(pingCommand.name() + Command.SEPARATOR + HOST + Command.SEPARATOR + REST_PORT);
		assertTrue(result.startsWith(HOST + ":" + REST_PORT));
	}

}
