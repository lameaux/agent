package com.euromoby.mail.command;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.mail.MailSession;

@RunWith(MockitoJUnitRunner.class)
public class RsetSmtpCommandTest {

	@Mock
	MailSession mailSession;
	
	RsetSmtpCommand command;

	@Before
	public void init() {
		command = new RsetSmtpCommand();
	}

	@Test
	public void testName() {
		assertEquals(RsetSmtpCommand.COMMAND_NAME, command.name());
	}

	@Test
	public void shouldBeOK() {
		assertEquals(RsetSmtpCommand.RESPONSE_250_OK, command.execute(mailSession, null));
		Mockito.verify(mailSession).reset();
	}

}
