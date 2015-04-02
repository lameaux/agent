package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommandProcessorTest {
	@Mock
	Command command1;
	@Mock
	Command command2;
	CommandProcessor commandProcessor;

	@Before
	public void init() {
		commandProcessor = new CommandProcessor();
		commandProcessor.setCommands(Arrays.asList(command1, command2));
	}
	
	@Test
	public void testInvalidCommand() {
		String requestString = "cmd";
		Mockito.when(command1.match(Matchers.eq(requestString))).thenReturn(false);
		Mockito.when(command2.match(Matchers.eq(requestString))).thenReturn(false);		
		assertEquals(CommandProcessor.INVALID_COMMAND, commandProcessor.process(requestString));
	}

	@Test
	public void testExecuteCommand() {
		String requestString = "cmd";
		Mockito.when(command1.match(Matchers.eq(requestString))).thenReturn(false);
		Mockito.when(command2.match(Matchers.eq(requestString))).thenReturn(true);
		String response = "OK";
		Mockito.when(command2.execute(Matchers.eq(requestString))).thenReturn(response);
		assertEquals(response, commandProcessor.process(requestString));
	}	
	
	@Test
	public void testException() {
		String requestString = "cmd";
		Mockito.when(command1.match(Matchers.eq(requestString))).thenReturn(false);
		Mockito.when(command2.match(Matchers.eq(requestString))).thenReturn(true);
		Mockito.when(command2.execute(Matchers.eq(requestString))).thenThrow(new RuntimeException());
		assertTrue(commandProcessor.process(requestString).startsWith("Error: "));
	}	
	
}
