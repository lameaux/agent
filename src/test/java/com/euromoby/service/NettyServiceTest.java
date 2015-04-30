package com.euromoby.service;

import static org.junit.Assert.assertEquals;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.Config;

@RunWith(MockitoJUnitRunner.class)
public class NettyServiceTest {

	private static final String SERVICE_NAME = "abstractService";	
	
	@Mock
	ChannelInitializer<SocketChannel> initializer;

	NettyService server;

	@Before
	public void init() {
		server = new NettyService(initializer) {

			@Override
			public String getServiceName() {
				return SERVICE_NAME;
			}

			@Override
			public int getPort() {
				return Integer.parseInt(Config.DEFAULT_AGENT_BASE_PORT);
			}
			
		};
	}

	@Test
	public void shouldBeStopped() {
		assertEquals(ServiceState.STOPPED, server.getServiceState());
	}

	@Test
	public void testGetServiceName() {
		assertEquals(SERVICE_NAME, server.getServiceName());
	}	

	@Test
	public void testStartAndStop() throws Exception {
		server.stopService();
		assertEquals(ServiceState.STOPPED, server.getServiceState());
		server.startService();
		assertEquals(ServiceState.RUNNING, server.getServiceState());
		server.startService();
		assertEquals(ServiceState.RUNNING, server.getServiceState());
		server.stopService();
		assertEquals(ServiceState.STOPPED, server.getServiceState());		
		server.stopService();
		assertEquals(ServiceState.STOPPED, server.getServiceState());		
	}
	
	
}
