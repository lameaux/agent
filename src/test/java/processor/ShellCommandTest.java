package processor;

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
	public void testMatch() {
		assertTrue(shell.match("shell"));
	}

	@Test
	public void testNotMatch() {
		assertFalse(shell.match("shellaaa"));
	}

	@Test
	public void testBadRequest() {
		String result = shell.execute("shellaaa");
		assertEquals(shell.syntaxError(), result);
	}

	@Test
	public void testGoodRequest() {
		String params = "java -version";
		String result = shell.execute(shell.name() + ShellCommand.COMMAND_SEPARATOR + params);
		assertTrue(result.contains("java"));
	}

}
