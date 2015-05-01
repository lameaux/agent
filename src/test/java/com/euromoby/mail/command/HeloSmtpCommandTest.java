package com.euromoby.mail.command;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.mail.MailSession;
import com.euromoby.model.Tuple;

public class HeloSmtpCommandTest {

	public static final String DOMAIN = "example.com";
	
	HeloSmtpCommand command;
	MailSession mailSession;
	Tuple<String, String> request;
	
	@Before
	public void init() {
		command = new HeloSmtpCommand();
		mailSession = new MailSession();
		request = Tuple.of(HeloSmtpCommand.COMMAND_NAME, DOMAIN);
	}

	@Test
	public void testName() {
		assertEquals(HeloSmtpCommand.COMMAND_NAME, command.name());
	}

	@Test
	public void errorOnInvalidDomain() {
		request.setSecond(null);
		assertEquals(HeloSmtpCommand.RESPONSE_501_INVALID_DOMAIN, command.execute(mailSession, request));
	}

	@Test
	public void shouldReturnOk() {
		assertEquals(HeloSmtpCommand.RESPONSE_250_HELLO+DOMAIN, command.execute(mailSession, request));
		assertEquals(DOMAIN, mailSession.getDomain());
	}

}
