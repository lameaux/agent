package com.euromoby.mail.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.mail.MailSession;
import com.euromoby.model.Tuple;

public class MailSmtpCommandTest {

	public static final String USER = "info";
	public static final String SERVER = "example.com";
	public static final int MESSAGE_SIZE = 12345;

	MailSmtpCommand command;
	MailSession mailSession;
	Tuple<String, String> request;

	@Before
	public void init() {
		command = new MailSmtpCommand();
		mailSession = new MailSession();
		mailSession.setDomain(SERVER);
		String line = "FROM: <" + USER + "@" + SERVER + "> SIZE=" + MESSAGE_SIZE;
		request = Tuple.of(MailSmtpCommand.COMMAND_NAME, line);
	}

	@Test
	public void testName() {
		assertEquals(MailSmtpCommand.COMMAND_NAME, command.name());
	}

	@Test
	public void errorOnMissingDomain() {
		mailSession.setDomain(null);
		assertEquals(MailSmtpCommand.RESPONSE_503_NEED_HELO, command.execute(mailSession, request));
	}

	@Test
	public void errorOnEmpty() {
		request.setSecond(null);
		assertEquals(MailSmtpCommand.RESPONSE_501_SYNTAX_ERROR_MAIL, command.execute(mailSession, request));
	}

	@Test
	public void errorOnSyntaxError() {
		request.setSecond("foo");
		assertEquals(MailSmtpCommand.RESPONSE_501_SYNTAX_ERROR_MAIL, command.execute(mailSession, request));
	}

	@Test
	public void errorOnSenderSyntaxError() {
		request.setSecond("FROM: <" + USER + "blah" + SERVER + "> SIZE=" + MESSAGE_SIZE);
		assertEquals(MailSmtpCommand.RESPONSE_501_SYNTAX_ERROR_SENDER, command.execute(mailSession, request));
	}

	@Test
	public void errorOnBigMessageSize() {
		request.setSecond("FROM: <" + USER + "@" + SERVER + "> SIZE=" + MailSession.MAX_MESSAGE_SIZE+1);
		assertEquals(MailSmtpCommand.RESPONSE_552_MESSAGE_SIZE, command.execute(mailSession, request));
	}	

	@Test
	public void shouldSetSenderAndReturnOk() {
		request.setSecond("FROM: <" + USER + "@" + SERVER + ">");
		assertEquals(MailSmtpCommand.RESPONSE_250_SENDER_OK, command.execute(mailSession, request));
		assertEquals(0, mailSession.getDeclaredMailSize());
		assertEquals(USER, mailSession.getSender().getFirst());
		assertEquals(SERVER, mailSession.getSender().getSecond());
	}	
	
	@Test
	public void shouldSetMessageSizeAndSenderAndReturnOk() {
		assertEquals(MailSmtpCommand.RESPONSE_250_SENDER_OK, command.execute(mailSession, request));
		assertEquals(MESSAGE_SIZE, mailSession.getDeclaredMailSize());
		assertEquals(USER, mailSession.getSender().getFirst());
		assertEquals(SERVER, mailSession.getSender().getSecond());
	}

	@Test
	public void shouldAllowUser() {
		assertTrue(command.isAllowedSender(null));
	}
	
}
