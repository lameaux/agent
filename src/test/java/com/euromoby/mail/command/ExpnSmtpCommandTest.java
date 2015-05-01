package com.euromoby.mail.command;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ExpnSmtpCommandTest {

	ExpnSmtpCommand command;

	@Before
	public void init() {
		command = new ExpnSmtpCommand();
	}

	@Test
	public void testName() {
		assertEquals(ExpnSmtpCommand.COMMAND_NAME, command.name());
	}

}
