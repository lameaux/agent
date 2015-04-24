package com.euromoby.job;

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

import com.euromoby.agent.Config;
import com.euromoby.service.ServiceState;

@RunWith(MockitoJUnitRunner.class)
public class JobSchedulerTest {

	@Mock
	Config config;
	@Mock
	JobManager jobManager;
	@Mock
	JobFactory jobFactory;
	@Mock
	JobDetail jobDetail;
	@Mock
	Job job;
	
	JobScheduler jobScheduler;

	@Before
	public void init() {
		Mockito.when(config.getJobPoolSize()).thenReturn(Integer.parseInt(Config.DEFAULT_JOB_POOL_SIZE));
		jobScheduler = new JobScheduler(config, jobManager, jobFactory);
	}
	
	@Test
	public void shouldBeStopped() {
		assertEquals(ServiceState.STOPPED, jobScheduler.getServiceState());
	}

	@Test
	public void testGetServiceName() {
		assertEquals(JobScheduler.SERVICE_NAME, jobScheduler.getServiceName());
	}

	@Test
	public void testStopService() {
		jobScheduler.stopService();
		assertTrue(jobScheduler.isInterrupted());		
	}	
	
	@Test
	public void shouldBeNoNewJobs() throws Exception {
		Mockito.when(jobManager.hasNewJob()).thenReturn(false);		
		jobScheduler.scheduleNextJobs();
		Mockito.verify(jobManager, Mockito.times(1)).hasNewJob();
		Mockito.verifyNoMoreInteractions(jobManager);
		Mockito.verifyNoMoreInteractions(jobFactory);		
	}

	@Test
	public void shouldBeNoCompletedJobs() throws Exception {
		jobScheduler.checkCompletedJobs();
		Mockito.verifyZeroInteractions(jobManager);
	}	
	
	@Test
	public void scheduleNewJobAndCheckForCompletion() throws Exception {
		Mockito.when(jobManager.hasNewJob()).thenReturn(true);
		Mockito.when(jobManager.getNextJob()).thenReturn(jobDetail);
		Mockito.when(jobFactory.createJob(Matchers.eq(jobDetail))).thenReturn(job);

		JobDetail jobDetailResult = new JobDetail();
		Mockito.when(job.call()).thenReturn(jobDetailResult);
		jobScheduler.scheduleNextJobs();
		Mockito.verify(jobManager, Mockito.times(2)).notify(Matchers.eq(jobDetail));
		Mockito.verify(jobDetail).setState(Matchers.eq(JobState.WAITING));
		
		Thread.sleep(JobScheduler.SLEEP_TIME);
		
		jobScheduler.checkCompletedJobs();
		Mockito.verify(jobManager, Mockito.times(1)).notify(Matchers.eq(jobDetailResult));		
	}

	@Test
	public void scheduleNewJobThrowException() throws Exception {
		Mockito.when(jobManager.hasNewJob()).thenReturn(true);
		Mockito.when(jobManager.getNextJob()).thenReturn(jobDetail);
		Exception e = new Exception("Exception"); 
		Mockito.when(jobFactory.createJob(Matchers.eq(jobDetail))).thenThrow(e);

		jobScheduler.scheduleNextJobs();
		Mockito.verify(jobManager, Mockito.times(2)).notify(Matchers.eq(jobDetail));

		Mockito.verify(jobDetail).setError(Matchers.eq(true));
		Mockito.verify(jobDetail).setMessage(Matchers.eq(e.getMessage()));		
		Mockito.verify(jobDetail).setState(Matchers.eq(JobState.FAILED));
	}	
	
	@Test
	public void startAndStop() throws Exception {
		jobScheduler.stopService();
		assertTrue(jobScheduler.isInterrupted());
		assertEquals(ServiceState.STOPPED, jobScheduler.getServiceState());		
		
		jobScheduler.startService();
		assertFalse(jobScheduler.isInterrupted());
		assertEquals(ServiceState.RUNNING, jobScheduler.getServiceState());	

		jobScheduler.startService();
		assertFalse(jobScheduler.isInterrupted());
		assertEquals(ServiceState.RUNNING, jobScheduler.getServiceState());			
		
		jobScheduler.stopService();
		assertTrue(jobScheduler.isInterrupted());
		assertEquals(ServiceState.STOPPED, jobScheduler.getServiceState());		

		jobScheduler.stopService();
		assertTrue(jobScheduler.isInterrupted());
		assertEquals(ServiceState.STOPPED, jobScheduler.getServiceState());			
	
	}
	
}
