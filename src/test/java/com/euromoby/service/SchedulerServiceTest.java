package com.euromoby.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerServiceTest {

	private static final String SERVICE_NAME = "abstractService";

	SchedulerService schedulerService;

	@Before
	public void init() {
		schedulerService = new SchedulerService() {
			@Override
			public String getServiceName() {
				return SERVICE_NAME;
			}

			@Override
			public void executeInternal() throws InterruptedException {
			}
		};
	}

	@Test
	public void shouldBeStopped() {
		assertEquals(ServiceState.STOPPED, schedulerService.getServiceState());
	}

	@Test
	public void testGetServiceName() {
		assertEquals(SERVICE_NAME, schedulerService.getServiceName());
	}

	@Test
	public void testStopService() {
		schedulerService.stopService();
		assertTrue(schedulerService.isInterrupted());
	}

	@Test
	public void startAndStop() throws Exception {
		schedulerService.stopService();
		assertTrue(schedulerService.isInterrupted());
		assertEquals(ServiceState.STOPPED, schedulerService.getServiceState());

		schedulerService.startService();
		assertFalse(schedulerService.isInterrupted());
		assertEquals(ServiceState.RUNNING, schedulerService.getServiceState());

		schedulerService.startService();
		assertFalse(schedulerService.isInterrupted());
		assertEquals(ServiceState.RUNNING, schedulerService.getServiceState());

		schedulerService.stopService();
		assertTrue(schedulerService.isInterrupted());
		assertEquals(ServiceState.STOPPED, schedulerService.getServiceState());

		schedulerService.stopService();
		assertTrue(schedulerService.isInterrupted());
		assertEquals(ServiceState.STOPPED, schedulerService.getServiceState());

	}

}
