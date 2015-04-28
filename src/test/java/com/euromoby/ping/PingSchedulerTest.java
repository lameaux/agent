package com.euromoby.ping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.AgentManager;
import com.euromoby.agent.Config;
import com.euromoby.model.AgentId;
import com.euromoby.model.PingInfo;
import com.euromoby.rest.RestServer;
import com.euromoby.service.ServiceState;

@RunWith(MockitoJUnitRunner.class)
public class PingSchedulerTest {

	@Mock
	Config config;
	@Mock
	AgentManager agentManager;
	@Mock
	PingSender pingSender;
	
	PingScheduler pingScheduler;

	@Before
	public void init() {
		Mockito.when(config.getPingPoolSize()).thenReturn(Integer.parseInt(Config.DEFAULT_PING_POOL_SIZE));
		pingScheduler = new PingScheduler(config, agentManager, pingSender);
	}
	
	@Test
	public void shouldBeStopped() {
		assertEquals(ServiceState.STOPPED, pingScheduler.getServiceState());
	}

	@Test
	public void testGetServiceName() {
		assertEquals(PingScheduler.SERVICE_NAME, pingScheduler.getServiceName());
	}

	@Test
	public void testStopService() {
		pingScheduler.stopService();
		assertTrue(pingScheduler.isInterrupted());		
	}	
	
	@Test
	public void shouldBeNoNewPings() throws Exception {
		Mockito.when(agentManager.getAllForPing()).thenReturn(Collections.<AgentId>emptyList());		
		pingScheduler.scheduleNextPings();
		Mockito.verify(agentManager, Mockito.times(1)).getAllForPing();
		Mockito.verifyNoMoreInteractions(agentManager);
	}

	@Test
	public void shouldBeNoCompletedJobs() throws Exception {
		pingScheduler.checkReceivedPings();
		Mockito.verifyZeroInteractions(agentManager);
	}	
	
	@Test
	public void scheduleNewPingAndCheckForCompletion() throws Exception {
		AgentId agent1 = new AgentId("agent1:21000");
		AgentId agent2 = new AgentId("agent2:21000");
		List<AgentId> agentList =  Arrays.asList(agent1, agent2);
		Mockito.when(agentManager.getAllForPing()).thenReturn(agentList);
		
		PingInfo pingInfo1 = new PingInfo(agent1);
		Mockito.when(pingSender.ping(Matchers.eq(agent1.getHost()), Matchers.eq(agent1.getBasePort() + RestServer.REST_PORT), Matchers.eq(false))).thenReturn(pingInfo1);

		PingInfo pingInfo2 = new PingInfo(agent2);
		Mockito.when(pingSender.ping(Matchers.eq(agent2.getHost()), Matchers.eq(agent2.getBasePort() + RestServer.REST_PORT), Matchers.eq(false))).thenReturn(pingInfo2);
		
		pingScheduler.scheduleNextPings();
		
		Thread.sleep(PingScheduler.SLEEP_TIME);
		
		Mockito.verify(agentManager, Mockito.times(agentList.size())).notifyPingSendAttempt(Matchers.any(AgentId.class));
		
		for (int i = 0; i < agentList.size(); i++) {
			pingScheduler.checkReceivedPings();
		}

		Mockito.verify(agentManager, Mockito.times(agentList.size())).notifyPingSendSuccess(Matchers.any(PingInfo.class));
	}

	@Test
	public void scheduleNewPingThrowException() throws Exception {
		AgentId agent1 = new AgentId("agent1:21000");
		Mockito.when(agentManager.getAllForPing()).thenReturn(Arrays.asList(agent1));

		Exception e = new Exception();
		Mockito.when(pingSender.ping(Matchers.eq(agent1.getHost()), Matchers.eq(agent1.getBasePort() + RestServer.REST_PORT), Matchers.eq(false))).thenThrow(e);
		
		pingScheduler.scheduleNextPings();

		Mockito.verify(agentManager, Mockito.times(1)).getAllForPing();
		
	}	
	
	@Test
	public void startAndStop() throws Exception {
		pingScheduler.stopService();
		assertTrue(pingScheduler.isInterrupted());
		assertEquals(ServiceState.STOPPED, pingScheduler.getServiceState());		
		
		pingScheduler.startService();
		assertFalse(pingScheduler.isInterrupted());
		assertEquals(ServiceState.RUNNING, pingScheduler.getServiceState());	

		pingScheduler.startService();
		assertFalse(pingScheduler.isInterrupted());
		assertEquals(ServiceState.RUNNING, pingScheduler.getServiceState());			
		
		pingScheduler.stopService();
		assertTrue(pingScheduler.isInterrupted());
		assertEquals(ServiceState.STOPPED, pingScheduler.getServiceState());		

		pingScheduler.stopService();
		assertTrue(pingScheduler.isInterrupted());
		assertEquals(ServiceState.STOPPED, pingScheduler.getServiceState());			
	
	}
	
}
