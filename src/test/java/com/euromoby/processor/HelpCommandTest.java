package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.utils.StringUtils;

@RunWith(MockitoJUnitRunner.class)
public class HelpCommandTest {
	@Mock
	Command command1;
	@Mock
	Command command2;
	HelpCommand help;

	@Before
	public void init() {
		help = new HelpCommand();
		help.setCommands(Arrays.asList(command1, command2));
	}

	@Test
	public void testMatchName() {
		assertTrue(help.match(HelpCommand.NAME));
	}

	@Test
	public void testMatchWithParam() {
		assertTrue(help.match(HelpCommand.NAME + Command.SEPARATOR + "param"));
	}
	
	@Test
	public void testNotMatch() {
		assertFalse(help.match(HelpCommand.NAME + "aaa"));
	}	
	
	@Test
	public void testInvalidCommand() {
		Mockito.when(command1.name()).thenReturn("");
		Mockito.when(command2.name()).thenReturn("");
		assertEquals(HelpCommand.COMMAND_NOT_FOUND, help.execute(help.name() + Command.SEPARATOR + "foo"));
	}

	@Test
	public void testDefault() {
		String cmd1 = "cmd1";
		String cmd2 = "cmd2";

		Mockito.when(command1.name()).thenReturn(cmd1);
		Mockito.when(command2.name()).thenReturn(cmd2);
		
		String response = HelpCommand.HELP_HEADER + StringUtils.CRLF;
		response += cmd1 + StringUtils.CRLF;
		response += cmd2 + StringUtils.CRLF;
		response += StringUtils.CRLF + HelpCommand.HELP_FOOTER + StringUtils.CRLF;
		assertEquals(response, help.execute(help.name() + Command.SEPARATOR));
	}	
	
	@Test
	public void testCommand() {
		String cmd1 = "cmd1";
		String cmd2 = "cmd2";
		String help1 = "help1";
		String help2 = "help2";

		Mockito.when(command1.name()).thenReturn(cmd1);
		Mockito.when(command2.name()).thenReturn(cmd2);
		
		Mockito.when(command1.help()).thenReturn(help1);
		Mockito.when(command2.help()).thenReturn(help2);
		
		assertEquals(help1, help.execute(help.name() + Command.SEPARATOR + cmd1));
		assertEquals(help2, help.execute(help.name() + Command.SEPARATOR + cmd2));
	}	
	
}
