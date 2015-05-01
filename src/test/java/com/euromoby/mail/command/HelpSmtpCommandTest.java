package com.euromoby.mail.command;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class HelpSmtpCommandTest {

	HelpSmtpCommand command;

	@Before
	public void init() {
		command = new HelpSmtpCommand();
	}

	@Test
	public void testName() {
		assertEquals(HelpSmtpCommand.COMMAND_NAME, command.name());
	}

}
