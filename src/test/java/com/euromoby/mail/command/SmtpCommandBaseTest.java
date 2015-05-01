package com.euromoby.mail.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.mail.util.DSNStatus;
import com.euromoby.model.Tuple;

public class SmtpCommandBaseTest {

	public static final String COMMAND_NAME = "abstractCommand";

	public static final String RESPONSE_502_NOT_SUPPORTED = "502 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.SYSTEM_NOT_CAPABLE)
			+ " Command is not supported";

	SmtpCommandBase command;

	@Before
	public void init() {
		command = new SmtpCommandBase() {
			@Override
			public String name() {
				return COMMAND_NAME;
			}
		};
	}

	@Test
	public void shouldBeUnsupported() {
		assertEquals(RESPONSE_502_NOT_SUPPORTED, command.execute(null, null));
	}

	@Test
	public void shouldNotMatch() {
		assertFalse(command.match(Tuple.<String, String>empty()));
		assertFalse(command.match(Tuple.of("foo", "bar")));
	}

	@Test
	public void shouldMatch() {
		assertTrue(command.match(Tuple.of(COMMAND_NAME, "bar")));
	}
}
