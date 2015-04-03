package com.euromoby.processor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.Config;

@RunWith(MockitoJUnitRunner.class)
public class ConfigCommandTest {

	@Mock
	Config config;
	ConfigCommand configCommand;

	@Before
	public void init() {
		configCommand = new ConfigCommand(config);
	}

	@Test
	public void testMatchName() {
		assertTrue(configCommand.match(ConfigCommand.NAME));
	}

	@Test
	public void testMatchWithParam() {
		assertTrue(configCommand.match(ConfigCommand.NAME + Command.SEPARATOR + "param"));
	}
	
	@Test
	public void testNotMatch() {
		assertFalse(configCommand.match(ConfigCommand.NAME + "aaa"));
	}	
	
	@Test
	public void testDefault() {
		Mockito.when(config.getProperties()).thenReturn(new Properties());
		configCommand.execute(configCommand.name());
		Mockito.verify(config).getProperties();
	}	
	
	@Test
	public void testParameter() {
		String parameter = "user.home";
		configCommand.execute(configCommand.name() + Command.SEPARATOR + parameter);
		Mockito.verify(config).get(Matchers.eq(parameter));
	}	
	
}
