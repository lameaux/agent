package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.utils.ShellExecutor;

@RunWith(MockitoJUnitRunner.class)
public class ShellCommandTest {

	@Mock
	ShellExecutor shellExecutor;
	ShellCommand shell;

	@Before
	public void init() {
		shell = new ShellCommand(shellExecutor);
	}

	@Test
	public void testMatchName() {
		assertTrue(shell.match(ShellCommand.NAME));
	}

	@Test
	public void testMatchWithParam() {
		assertTrue(shell.match(ShellCommand.NAME + Command.SEPARATOR + "param"));
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
	public void testGoodRequest() throws Exception {
		String params = "java -version";
		String output = "java";
		Mockito.when(shellExecutor.executeCommandLine(Matchers.eq(params.split(" ")), 
				Matchers.eq(ShellCommand.TIMEOUT))).thenReturn(output);
		String result = shell.execute(shell.name() + Command.SEPARATOR + params);
		assertEquals(output, result);
	}

}
