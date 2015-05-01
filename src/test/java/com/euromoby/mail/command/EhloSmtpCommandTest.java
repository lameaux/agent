package com.euromoby.mail.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.Config;
import com.euromoby.mail.MailSession;
import com.euromoby.model.AgentId;
import com.euromoby.model.Tuple;

@RunWith(MockitoJUnitRunner.class)
public class EhloSmtpCommandTest {

	public static final String DOMAIN = "example.com";
	public static final String AGENT_HOST = "localhost";
	
	EhloSmtpCommand command;
	MailSession mailSession;
	Tuple<String, String> request;
	@Mock
	Config config;
	
	@Before
	public void init() {
		command = new EhloSmtpCommand(config);
		mailSession = new MailSession();
		request = Tuple.of(EhloSmtpCommand.COMMAND_NAME, DOMAIN);
	}

	@Test
	public void testName() {
		assertEquals(EhloSmtpCommand.COMMAND_NAME, command.name());
	}

	@Test
	public void errorOnInvalidDomain() {
		request.setSecond(null);
		assertEquals(EhloSmtpCommand.RESPONSE_501_INVALID_DOMAIN, command.execute(mailSession, request));
	}

	@Test
	public void shouldReturnOk() {
		Mockito.when(config.getAgentId()).thenReturn(new AgentId(AGENT_HOST, 21000));
		String result = command.execute(mailSession, request);
		assertTrue(result.startsWith(EhloSmtpCommand.RESPONSE_250_DASH+AGENT_HOST));
		assertEquals(DOMAIN, mailSession.getDomain());
	}

}
