package com.euromoby.mail.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.mail.MailSession;
import com.euromoby.model.Tuple;

public class DataSmtpCommandTest {

	DataSmtpCommand command;
	MailSession mailSession;

	@Before
	public void init() {
		command = new DataSmtpCommand();
		mailSession = new MailSession();
	}

	@Test
	public void testName() {
		assertEquals(DataSmtpCommand.COMMAND_NAME, command.name());
	}

	@Test
	public void errorOnMissingRecipient() {
		assertEquals(DataSmtpCommand.RESPONSE_503_NO_RECIPIENTS, command.execute(mailSession, null));
	}

	@Test
	public void shouldExitCommandModeAndReturnOk() {
		mailSession.setRecipient(Tuple.of("user", "example.com"));
		assertTrue(mailSession.isCommandMode());
		assertEquals(DataSmtpCommand.RESPONSE_354_OK, command.execute(mailSession, null));
		assertFalse(mailSession.isCommandMode());
	}

}
