package com.euromoby.mail.command;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class VrfySmtpCommandTest {

	VrfySmtpCommand command;

	@Before
	public void init() {
		command = new VrfySmtpCommand();
	}

	@Test
	public void testName() {
		assertEquals(VrfySmtpCommand.COMMAND_NAME, command.name());
	}

}
