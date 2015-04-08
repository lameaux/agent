package com.euromoby.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.euromoby.model.AgentId;

public class JobDetailTest {

	@Test
	public void testConstructors() {
		JobDetail jobDetailNoArgs = new JobDetail();
		JobDetail copy = new JobDetail(jobDetailNoArgs);
		assertEquals(jobDetailNoArgs, copy);

		Map<String, String> params = new HashMap<String, String>();
		JobDetail jobDetailWithArgs = new JobDetail(Job.class, params);
		assertEquals(Job.class.getCanonicalName(), jobDetailWithArgs.getJobClass());
		assertEquals(params, jobDetailWithArgs.getParameters());
	}

	@Test
	public void testMerge() {
		JobDetail job = new JobDetail();
		JobDetail copy = new JobDetail(job);
		copy.setState(JobState.FINISHED);
		copy.setStartTime(123);
		copy.setFinishTime(456);
		copy.setError(true);
		copy.setMessage("foo");

		assertNotEquals(copy.getState(), job.getState());
		assertNotEquals(copy.getStartTime(), job.getStartTime());
		assertNotEquals(copy.getFinishTime(), job.getFinishTime());
		assertNotEquals(copy.isError(), job.isError());
		assertNotEquals(copy.getMessage(), job.getMessage());

		job.merge(copy);

		assertEquals(copy.getState(), job.getState());
		assertEquals(copy.getStartTime(), job.getStartTime());
		assertEquals(copy.getFinishTime(), job.getFinishTime());
		assertEquals(copy.isError(), job.isError());
		assertEquals(copy.getMessage(), job.getMessage());

	}

	@Test
	public void testGetSet() {

		JobDetail job = new JobDetail();

		AgentId sender = new AgentId("sender:123");
		job.setSender(sender);
		assertEquals(sender, job.getSender());

		AgentId recipient = new AgentId("recipient:123");
		job.setRecipient(recipient);
		assertEquals(recipient, job.getRecipient());

		UUID uuid = UUID.randomUUID();
		job.setUuid(uuid);
		assertEquals(uuid, job.getUuid());

		String jobClass = Job.class.getCanonicalName();
		job.setJobClass(jobClass);
		assertEquals(jobClass, job.getJobClass());

		long scheduleTime = System.currentTimeMillis();
		job.setScheduleTime(scheduleTime);
		assertEquals(scheduleTime, job.getScheduleTime());

		Map<String, String> parameters = new HashMap<String, String>();
		job.setParameters(parameters);
		assertEquals(parameters, job.getParameters());

		JobState state = JobState.NEW;
		job.setState(state);
		assertEquals(state, job.getState());

		long startTime = System.currentTimeMillis();
		job.setStartTime(startTime);
		assertEquals(startTime, job.getStartTime());

		long finishTime = System.currentTimeMillis();
		job.setFinishTime(finishTime);
		assertEquals(finishTime, job.getFinishTime());

		boolean error = true;
		job.setError(error);
		assertEquals(error, job.isError());

		String message = "foo";
		job.setMessage(message);
		assertEquals(message, job.getMessage());

	}

	@Test
	public void testCanStartNow() {
		JobDetail job = new JobDetail();
		job.setScheduleTime(Long.MAX_VALUE);
		assertFalse(job.canStartNow());

		job.setScheduleTime(System.currentTimeMillis() - 1);
		assertTrue(job.canStartNow());
	}

	@Test
	public void testCompareTo() {
		JobDetail job1 = new JobDetail();
		JobDetail job2 = new JobDetail();

		job1.setScheduleTime(1);
		job2.setScheduleTime(1);
		assertEquals(0, job1.compareTo(job2));

		job1.setScheduleTime(1);
		job2.setScheduleTime(2);
		assertEquals(-1, job1.compareTo(job2));

		job1.setScheduleTime(2);
		job2.setScheduleTime(1);
		assertEquals(1, job1.compareTo(job2));

	}

	@Test
	public void testExtra() {
		JobDetail job = new JobDetail();
		JobDetail job2 = new JobDetail();		
		job.hashCode();
		job.toString();
		
		assertFalse(job.equals(null));
		assertFalse(job.equals("foo"));		
		assertTrue(job.equals(job));
		assertFalse(job.equals(job2));
		job2.setUuid(job.getUuid());
		assertTrue(job.equals(job2));
		job.setUuid(null);
		assertFalse(job.equals(job2));		
		job2.setUuid(null);
		assertTrue(job.equals(job2));		
		
	}

}
