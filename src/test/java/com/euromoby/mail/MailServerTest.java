package com.euromoby.mail;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.Config;

@RunWith(MockitoJUnitRunner.class)
public class MailServerTest {

	@Mock
	Config config;
	@Mock
	MailServerInitializer mailServerInitializer;

	MailServer server;

	@Before
	public void init() {
		server = new MailServer(mailServerInitializer);
	}

	@Test
	public void testGetServiceName() {
		assertEquals(MailServer.SERVICE_NAME, server.getServiceName());
	}

	@Test
	public void testGetPort() {
		assertEquals(MailServer.MAIL_PORT, server.getPort());
	}
	
}
