package com.euromoby.processor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.utils.StringUtils;

@RunWith(MockitoJUnitRunner.class)
public class CommandBaseTest {

	private static final String COMMAND_NAME = "command";
	
	CommandBase commandBase;

	@Before
	public void init() {
		commandBase = new CommandBase() {

			@Override
			public String name() {
				return COMMAND_NAME;
			}
			
		};
	}

	@Test
	public void testExecute() {
		assertNull(commandBase.execute(null));
	}

	@Test
	public void testHelp() {
		assertEquals(commandBase.name(), commandBase.help());
	}	
	
	@Test
	public void testMatchNull() {
		assertFalse(commandBase.match(null));
	}

	@Test
	public void testMatch() {
		assertTrue(commandBase.match(COMMAND_NAME));
		assertTrue(commandBase.match(COMMAND_NAME + Command.SEPARATOR + "bla"));
	}	

	@Test
	public void testNotMatch() {
		assertFalse(commandBase.match("bla"));
	}	
	
	
	@Test
	public void testSyntaxError() {
		assertEquals(CommandBase.SYNTAX_ERROR + StringUtils.CRLF + commandBase.help(), commandBase.syntaxError());
	}
	
	@Test
	public void testParameters() {
		assertArrayEquals(new String[0], commandBase.parameters("blah"));
		assertArrayEquals(new String[]{"foo"}, commandBase.parameters(COMMAND_NAME + Command.SEPARATOR + "foo"));
		assertArrayEquals(new String[]{"foo", "bar"}, commandBase.parameters(COMMAND_NAME + Command.SEPARATOR + "foo" + Command.SEPARATOR + "bar"));		
	}
	
	
}
