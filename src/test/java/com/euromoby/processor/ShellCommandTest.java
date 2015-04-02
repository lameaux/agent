package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ShellCommandTest {

	ShellCommand shell;

	@Before
	public void init() {
		shell = new ShellCommand();
	}

	@Test
	public void testMatchName() {
		assertTrue(shell.match(ShellCommand.NAME));
	}

	@Test
	public void testMatchWithParam() {
		assertTrue(shell.match(ShellCommand.NAME + CommandBase.COMMAND_SEPARATOR + "param"));
	}
	
	@Test
	public void testNotMatch() {
		assertFalse(shell.match(ShellCommand.NAME + "aaa"));
	}

	@Test
	public void testBadRequest() {
		String result = shell.execute(ShellCommand.NAME + "");
		assertEquals(shell.syntaxError(), result);
	}

	@Test
	public void testGoodRequest() {
		String params = "java -version";
		String result = shell.execute(shell.name() + CommandBase.COMMAND_SEPARATOR + params);
		assertTrue(result.toLowerCase().contains("java"));
	}

}
