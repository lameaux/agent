package com.euromoby.processor;

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

import com.euromoby.job.JobDetail;
import com.euromoby.job.JobManager;

@RunWith(MockitoJUnitRunner.class)
public class UploadCommandTest {

	private static final String URL = "http://example.com";
	private static final String LOCATION = "file";

	@Mock
	JobManager jobManager;
	UploadCommand uploadCommand;

	@Before
	public void init() {
		uploadCommand = new UploadCommand(jobManager);
	}

	@Test
	public void testMatchName() {
		assertTrue(uploadCommand.match(UploadCommand.NAME));
	}

	@Test
	public void testMatchWithParam() {
		assertTrue(uploadCommand.match(UploadCommand.NAME + Command.SEPARATOR + LOCATION + Command.SEPARATOR + URL));
		assertTrue(uploadCommand.match(UploadCommand.NAME + Command.SEPARATOR + LOCATION + Command.SEPARATOR + URL + Command.SEPARATOR
				+ UploadCommand.NO_PROXY));
	}

	@Test
	public void testNotMatch() {
		assertFalse(uploadCommand.match(UploadCommand.NAME + "aaa"));
	}

	@Test
	public void testBadRequest() {
		String result = uploadCommand.execute(UploadCommand.NAME + Command.SEPARATOR + "aaa");
		assertEquals(uploadCommand.syntaxError(), result);
	}

	@Test
	public void testWithProxy() {
		String result = uploadCommand.execute(uploadCommand.name() + Command.SEPARATOR + LOCATION + Command.SEPARATOR + URL);
		Mockito.verify(jobManager).submit(Matchers.any(JobDetail.class));
		assertFalse(uploadCommand.syntaxError().equals(result));
	}

	@Test
	public void testWithoutProxy() {
		String result = uploadCommand.execute(uploadCommand.name() + Command.SEPARATOR + LOCATION + Command.SEPARATOR + URL + Command.SEPARATOR + UploadCommand.NO_PROXY);
		Mockito.verify(jobManager).submit(Matchers.any(JobDetail.class));
		assertFalse(uploadCommand.syntaxError().equals(result));
	}	
}
