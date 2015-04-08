package com.euromoby.processor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.Config;
import com.euromoby.utils.SystemUtils;

@RunWith(MockitoJUnitRunner.class)
public class FreeSpaceCommandTest {

	@Mock
	Config config;
	FreeSpaceCommand freeSpaceCommand;

	@Before
	public void init() {
		freeSpaceCommand = new FreeSpaceCommand(config);
	}

	@Test
	public void testMatchName() {
		assertTrue(freeSpaceCommand.match(FreeSpaceCommand.NAME));
	}
	
	@Test
	public void testNotMatch() {
		assertFalse(freeSpaceCommand.match(FreeSpaceCommand.NAME + "aaa"));
	}

	@Test
	public void testDefault() {
		Mockito.when(config.getAgentRootPath()).thenReturn(SystemUtils.getUserHome());
		String result = freeSpaceCommand.execute(freeSpaceCommand.name());
		assertTrue(result.matches("[0-9]+G [0-9]+M"));
	}

	
}
