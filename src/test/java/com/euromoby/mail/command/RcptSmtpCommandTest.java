package com.euromoby.mail.command;

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

import com.euromoby.mail.MailAccount;
import com.euromoby.mail.MailManager;
import com.euromoby.mail.MailSession;
import com.euromoby.model.Tuple;

@RunWith(MockitoJUnitRunner.class)
public class RcptSmtpCommandTest {

	public static final String USER = "info";
	public static final String SERVER = "example.com";
	public static final int MESSAGE_SIZE = 12345;
	public static final Tuple<String, String> recipient = Tuple.of(USER, SERVER);

	RcptSmtpCommand command;
	MailSession mailSession;
	Tuple<String, String> request;
	
	@Mock
	MailManager mailManager;

	@Before
	public void init() {
		command = new RcptSmtpCommand(mailManager);
		mailSession = new MailSession();
		mailSession.setDomain(SERVER);
		mailSession.setSender(Tuple.of("foo", "bar"));
		String line = "TO: <" + USER + "@" + SERVER + ">";
		request = Tuple.of(RcptSmtpCommand.COMMAND_NAME, line);
	}

	@Test
	public void testName() {
		assertEquals(RcptSmtpCommand.COMMAND_NAME, command.name());
	}

	@Test
	public void errorOnMissingSender() {
		mailSession.setSender(Tuple.<String, String>empty());
		assertEquals(RcptSmtpCommand.RESPONSE_503_NEED_MAIL, command.execute(mailSession, request));
	}

	@Test
	public void errorOnEmpty() {
		request.setSecond(null);
		assertEquals(RcptSmtpCommand.RESPONSE_501_SYNTAX_ERROR, command.execute(mailSession, request));
	}

	@Test
	public void errorOnSyntaxError() {
		request.setSecond("foo");
		assertEquals(RcptSmtpCommand.RESPONSE_501_SYNTAX_ERROR, command.execute(mailSession, request));
	}

	@Test
	public void errorOnSenderSyntaxError() {
		request.setSecond("TO: <" + USER + "blah" + SERVER + ">");
		assertEquals(RcptSmtpCommand.RESPONSE_553_SYNTAX_ERROR_RECIPIENT, command.execute(mailSession, request));
	}

	

	@Test
	public void shouldSetRecipientAndReturnOk() {
		Mockito.when(mailManager.find(Matchers.refEq(recipient))).thenReturn(new MailAccount());		
		request.setSecond("TO: <" + USER + "@" + SERVER + ">");
		assertEquals(RcptSmtpCommand.RESPONSE_250_RECIPIENT_OK, command.execute(mailSession, request));
		assertEquals(USER, mailSession.getRecipient().getFirst());
		assertEquals(SERVER, mailSession.getRecipient().getSecond());
	}	

	@Test
	public void shouldAllowUser() {
		Mockito.when(mailManager.find(Matchers.eq(recipient))).thenReturn(new MailAccount());
		assertTrue(command.isAllowedRecipient(recipient));
	}

	@Test
	public void shouldNotAllowUser() {
		Mockito.when(mailManager.find(Matchers.eq(recipient))).thenReturn(null);
		assertFalse(command.isAllowedRecipient(recipient));
	}	
	
}
