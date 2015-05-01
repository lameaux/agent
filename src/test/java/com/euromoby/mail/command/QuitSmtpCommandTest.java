package com.euromoby.mail.command;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class QuitSmtpCommandTest {

	QuitSmtpCommand command;

	@Before
	public void init() {
		command = new QuitSmtpCommand();
	}

	@Test
	public void testName() {
		assertEquals(QuitSmtpCommand.COMMAND_NAME, command.name());
	}

	@Test
	public void shouldBeOK() {
		assertEquals(QuitSmtpCommand.RESPONSE_221_BYE, command.execute(null, null));
	}

}
