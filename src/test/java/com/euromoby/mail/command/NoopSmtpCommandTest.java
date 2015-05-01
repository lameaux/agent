package com.euromoby.mail.command;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class NoopSmtpCommandTest {

	NoopSmtpCommand command;

	@Before
	public void init() {
		command = new NoopSmtpCommand();
	}

	@Test
	public void testName() {
		assertEquals(NoopSmtpCommand.COMMAND_NAME, command.name());
	}

	@Test
	public void shouldBeOK() {
		assertEquals(NoopSmtpCommand.RESPONSE_250_OK, command.execute(null, null));
	}

}
