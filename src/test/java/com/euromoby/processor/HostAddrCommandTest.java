package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.Config;

@RunWith(MockitoJUnitRunner.class)
public class HostAddrCommandTest {

	@Mock
	Config config;
	HostAddrCommand hostAddrCommand;

	@Before
	public void init() {
		hostAddrCommand = new HostAddrCommand(config);
	}

	@Test
	public void testMatchName() {
		assertTrue(hostAddrCommand.match(HostAddrCommand.NAME));
	}
	
	@Test
	public void testNotMatch() {
		assertFalse(hostAddrCommand.match(HostAddrCommand.NAME + "aaa"));
	}

	@Test
	public void testDefault() {
		String host = "host";
		Mockito.when(config.getHost()).thenReturn(host);
		String result = hostAddrCommand.execute(hostAddrCommand.name());
		assertEquals(host, result);
	}

	
}
