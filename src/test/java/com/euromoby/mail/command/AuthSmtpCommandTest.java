package com.euromoby.mail.command;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class AuthSmtpCommandTest {

	AuthSmtpCommand command;

	@Before
	public void init() {
		command = new AuthSmtpCommand();
	}

	@Test
	public void testName() {
		assertEquals(AuthSmtpCommand.COMMAND_NAME, command.name());
	}

}
